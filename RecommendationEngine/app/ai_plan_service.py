import json
import os
import re
from datetime import date
from typing import Any, Dict, List, Optional
from urllib.parse import quote_plus

from google import genai
from google.genai import types


class AiPlanService:
    def get_ai_plans(
        self,
        location_name: str,
        category: str,
        weather_summary: str,
        latitude: Optional[float] = None,
        longitude: Optional[float] = None,
        temperature: Optional[float] = None,
        rain: Optional[int] = None,
        wind: Optional[float] = None,
        reliability: Optional[str] = None,
        status: Optional[str] = None
    ) -> List[Dict[str, Any]]:
        fallback_plans = self.get_fallback_plans(
            location_name=location_name,
            category=category,
            latitude=latitude,
            longitude=longitude,
            temperature=temperature,
            rain=rain,
            wind=wind,
            reliability=reliability,
            status=status
        )

        api_key = os.getenv("GEMINI_API_KEY")

        if not api_key:
            return fallback_plans

        try:
            client = genai.Client(api_key=api_key)

            grounding_tool = types.Tool(
                google_search=types.GoogleSearch()
            )

            prompt = self._build_prompt(
                location_name=location_name,
                category=category,
                weather_summary=weather_summary,
                latitude=latitude,
                longitude=longitude,
                temperature=temperature,
                rain=rain,
                wind=wind,
                reliability=reliability,
                status=status
            )

            response = client.models.generate_content(
                model="gemini-2.5-flash",
                contents=prompt,
                config=types.GenerateContentConfig(
                    tools=[grounding_tool]
                )
            )

            ai_plans = self._parse_ai_response(response.text)
            normalized_plans = self._normalize_plans(ai_plans)

            if not normalized_plans:
                return fallback_plans

            return self._merge_plans(normalized_plans, fallback_plans)

        except Exception:
            return fallback_plans

    def _build_prompt(
        self,
        location_name: str,
        category: str,
        weather_summary: str,
        latitude: Optional[float],
        longitude: Optional[float],
        temperature: Optional[float],
        rain: Optional[int],
        wind: Optional[float],
        reliability: Optional[str],
        status: Optional[str]
    ) -> str:
        return f"""
Devuelve únicamente JSON válido. No uses Markdown. No expliques nada fuera del JSON.

Fecha actual: {date.today().isoformat()}
Ubicación: {location_name}
Latitud: {latitude}
Longitud: {longitude}
Categoría meteorológica recomendada: {category}
Temperatura: {temperature}
Probabilidad de lluvia: {rain}
Viento: {wind}
Fiabilidad: {reliability}
Estado meteorológico: {status}
Resumen meteorológico: {weather_summary}

Necesito entre 5 y 8 planes personalizados cerca de la ubicación.

Reglas:
Cada plan debe incluir un ejemplo concreto en placeName.
No devuelvas planes genéricos tipo "bar cercano", "restaurante cercano", "cine cercano" o "buscar en Google".
No repitas el mismo tipo de plan. No devuelvas dos planes de cine, dos restaurantes o dos planes culturales similares.
Incluye variedad real: cine, eventos, cultura, cafetería, restaurante, escape room, realidad virtual, recreativos, bolera, centro comercial, parque, ruta, mirador, playa o paseo marítimo según el tiempo.
Si el tiempo obliga a planes cubiertos, no uses siempre cine. Alterna con escape rooms, realidad virtual, recreativos, bolera, centros comerciales, museos, cafeterías o eventos cubiertos.
Si recomiendas bares o restaurantes, usa una zona concreta o un establecimiento concreto si lo puedes verificar.
Si recomiendas cine, usa un cine concreto, pero no inventes películas ni horarios.
Si recomiendas eventos, usa un recinto, agenda o espacio concreto, pero no inventes eventos si no están verificados.
Si llueve, hay tormenta, hace frío o hay viento fuerte, prioriza planes de interior.
Si hace buen tiempo, incluye planes exteriores, rutas suaves, parques, miradores, paseos urbanos, playa o zonas de agua.
Si hace calor, evita esfuerzos intensos, pero no descartes la playa. Bañarse o pasar la tarde cerca del agua puede ser un plan prioritario.
Si es verano y hace buen tiempo o calor, incluye planes de playa, baño, paseo marítimo o zonas de agua cuando la ubicación tenga costa o una playa cercana.
No recomiendes playa si llueve, hay tormenta, viento fuerte o mala previsión.
Si la ubicación es Bilbao, Barakaldo o alrededores, puedes incluir BEC, Guggenheim, Azkuna Zentroa, Zubiarte, Max Ocio, Max Center, Parque Doña Casilda, Artxanda, Casco Viejo, Plaza Nueva, Ereaga, Arrigunaga o Sopela cuando encaje.
El campo externalUrl debe servir para ampliar información, consultar horarios, cartelera, agenda, bandera de playa o alternativas.

Estructura obligatoria:
{{
  "personalizedPlans": [
    {{
      "title": "string",
      "category": "INTERIOR | EXTERIOR | MIXTO | PRECAUCION | OCIO | CULTURA | EVENTOS | RESTAURACION",
      "description": "string",
      "placeName": "string",
      "address": "string",
      "externalUrl": "string",
      "source": "AI_SEARCH"
    }}
  ]
}}
"""

    def _parse_ai_response(self, text: Optional[str]) -> List[Dict[str, Any]]:
        if not text:
            return []

        clean_text = text.strip()
        clean_text = clean_text.replace("```json", "")
        clean_text = clean_text.replace("```", "")
        clean_text = clean_text.strip()

        match = re.search(r"\{.*\}", clean_text, re.DOTALL)

        if match:
            clean_text = match.group(0)

        data = json.loads(clean_text)

        plans = data.get("personalizedPlans")

        if plans is None:
            plans = data.get("plans")

        if not isinstance(plans, list):
            return []

        return plans

    def _normalize_plans(self, plans: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        normalized = []

        for plan in plans:
            if not isinstance(plan, dict):
                continue

            title = self._safe_string(plan.get("title"))
            description = self._safe_string(plan.get("description"))
            category = self._safe_string(plan.get("category")) or "MIXTO"
            place_name = self._safe_string(plan.get("placeName")) or self._safe_string(plan.get("place_name"))
            address = self._safe_string(plan.get("address"))
            external_url = self._safe_string(plan.get("externalUrl")) or self._safe_string(plan.get("url"))
            source = self._safe_string(plan.get("source")) or "AI_SEARCH"

            if not title or not description:
                continue

            if self._is_too_generic_plan(title, place_name):
                continue

            if not external_url:
                query = f"{title} {place_name} {address}".strip()
                external_url = self._build_google_search_url(query)

            normalized.append({
                "title": title,
                "category": category.upper(),
                "description": description,
                "placeName": place_name,
                "address": address,
                "externalUrl": external_url,
                "source": source
            })

        return normalized[:8]

    def get_fallback_plans(
        self,
        location_name: str,
        category: str,
        latitude: Optional[float] = None,
        longitude: Optional[float] = None,
        temperature: Optional[float] = None,
        rain: Optional[int] = None,
        wind: Optional[float] = None,
        reliability: Optional[str] = None,
        status: Optional[str] = None
    ) -> List[Dict[str, Any]]:
        location = location_name or "la ubicación seleccionada"
        normalized_category = (category or "MIXTO").upper()
        normalized_status = (status or "").lower()
        normalized_reliability = (reliability or "MEDIA").upper()

        bad_weather = (
            normalized_category in ["INTERIOR", "PRECAUCION"]
            or (rain is not None and rain >= 50)
            or (wind is not None and wind >= 30)
            or (temperature is not None and temperature <= 8)
            or "lluvia" in normalized_status
            or "llovizna" in normalized_status
            or "tormenta" in normalized_status
            or "storm" in normalized_status
            or "rain" in normalized_status
        )

        hot_weather = temperature is not None and temperature >= 30

        if normalized_category == "EXTERIOR" and not bad_weather and not hot_weather:
            plans = self._build_exterior_fallback(location, latitude, longitude)
        elif bad_weather:
            plans = self._build_interior_fallback(location, latitude, longitude)
        elif hot_weather:
            plans = self._build_hot_weather_fallback(location, latitude, longitude)
        else:
            plans = self._build_mixed_fallback(location, latitude, longitude)

        if self._is_bilbao_area(location, latitude, longitude):
            plans = self._add_bilbao_area_plans(plans, location, latitude, longitude)

        beach_plan = self._get_beach_plan_if_suitable(
            location=location,
            latitude=latitude,
            longitude=longitude,
            temperature=temperature,
            rain=rain,
            wind=wind,
            status=status,
            bad_weather=bad_weather
        )

        if beach_plan is not None:
            plans.insert(0, beach_plan)

        if normalized_reliability != "ALTA":
            alternative_plan = self._get_non_repeated_covered_plan(location, latitude, longitude, plans)

            if alternative_plan is not None:
                plans.append(alternative_plan)

        return self._remove_duplicates(plans)[:8]

    def _build_interior_fallback(
        self,
        location: str,
        latitude: Optional[float],
        longitude: Optional[float]
    ) -> List[Dict[str, Any]]:
        examples = self._get_local_examples(location, latitude, longitude)

        return [
            self._create_plan(
                "Agenda de eventos o exposiciones",
                "EVENTOS",
                "Buena alternativa cubierta para revisar ferias, exposiciones, conciertos o actividades activas hoy.",
                examples["eventos"]["placeName"],
                examples["eventos"]["address"],
                examples["eventos"]["query"],
                "FALLBACK"
            ),
            self._create_plan(
                "Visita cultural cubierta",
                "CULTURA",
                "Plan adecuado para lluvia, frío o viento, sin depender de una actividad exterior.",
                examples["cultura"]["placeName"],
                examples["cultura"]["address"],
                examples["cultura"]["query"],
                "FALLBACK"
            ),
            self._create_plan(
                "Escape room",
                "INTERIOR",
                "Plan cubierto más activo que el cine, recomendable para lluvia, viento o previsión dudosa.",
                examples["escape"]["placeName"],
                examples["escape"]["address"],
                examples["escape"]["query"],
                "FALLBACK"
            ),
            self._create_plan(
                "Experiencia de realidad virtual",
                "INTERIOR",
                "Alternativa cubierta de ocio tecnológico para hacer algo diferente sin depender del tiempo.",
                examples["realidad_virtual"]["placeName"],
                examples["realidad_virtual"]["address"],
                examples["realidad_virtual"]["query"],
                "FALLBACK"
            ),
            self._create_plan(
                "Café o bar tranquilo",
                "OCIO",
                "Plan flexible para pasar un rato cómodo en interior y evitar depender del tiempo exterior.",
                examples["bar"]["placeName"],
                examples["bar"]["address"],
                examples["bar"]["query"],
                "FALLBACK"
            ),
            self._create_plan(
                "Comida o cena en zona recomendada",
                "RESTAURACION",
                "Opción estable para completar el plan aunque el tiempo no acompañe.",
                examples["restaurante"]["placeName"],
                examples["restaurante"]["address"],
                examples["restaurante"]["query"],
                "FALLBACK"
            )
        ]

    def _build_exterior_fallback(
        self,
        location: str,
        latitude: Optional[float],
        longitude: Optional[float]
    ) -> List[Dict[str, Any]]:
        examples = self._get_local_examples(location, latitude, longitude)

        return [
            self._create_plan(
                "Paseo urbano recomendado",
                "EXTERIOR",
                "Las condiciones permiten una ruta sencilla sin exposición excesiva.",
                examples["ruta"]["placeName"],
                examples["ruta"]["address"],
                examples["ruta"]["query"],
                "FALLBACK"
            ),
            self._create_plan(
                "Parque o zona verde",
                "EXTERIOR",
                "Plan recomendable con buen tiempo y viento bajo o moderado.",
                examples["parque"]["placeName"],
                examples["parque"]["address"],
                examples["parque"]["query"],
                "FALLBACK"
            ),
            self._create_plan(
                "Sitio de interés cercano",
                "EXTERIOR",
                "Buena opción para visitar un punto destacado de la zona.",
                examples["interes"]["placeName"],
                examples["interes"]["address"],
                examples["interes"]["query"],
                "FALLBACK"
            ),
            self._create_plan(
                "Terraza o zona de bares",
                "OCIO",
                "Plan exterior ligero con posibilidad de parar en un sitio cercano.",
                examples["bar"]["placeName"],
                examples["bar"]["address"],
                examples["bar"]["query"],
                "FALLBACK"
            ),
            self._create_plan(
                "Restaurante en zona cercana",
                "RESTAURACION",
                "Opción compatible con un paseo exterior y una parada posterior.",
                examples["restaurante"]["placeName"],
                examples["restaurante"]["address"],
                examples["restaurante"]["query"],
                "FALLBACK"
            )
        ]

    def _build_hot_weather_fallback(
        self,
        location: str,
        latitude: Optional[float],
        longitude: Optional[float]
    ) -> List[Dict[str, Any]]:
        examples = self._get_local_examples(location, latitude, longitude)

        return [
            self._create_plan(
                "Paseo corto con sombra",
                "EXTERIOR",
                "Plan exterior suave, evitando esfuerzos intensos y las horas centrales del día.",
                examples["parque"]["placeName"],
                examples["parque"]["address"],
                examples["parque"]["query"],
                "FALLBACK"
            ),
            self._create_plan(
                "Actividad interior climatizada",
                "INTERIOR",
                "Alternativa recomendable si la temperatura resulta incómoda.",
                examples["centro_comercial"]["placeName"],
                examples["centro_comercial"]["address"],
                examples["centro_comercial"]["query"],
                "FALLBACK"
            ),
            self._create_plan(
                "Café o terraza",
                "OCIO",
                "Plan ligero para aprovechar el día sin exposición prolongada al calor.",
                examples["bar"]["placeName"],
                examples["bar"]["address"],
                examples["bar"]["query"],
                "FALLBACK"
            ),
            self._create_plan(
                "Comida o cena sin prisas",
                "RESTAURACION",
                "Opción cómoda para evitar actividad física intensa con calor.",
                examples["restaurante"]["placeName"],
                examples["restaurante"]["address"],
                examples["restaurante"]["query"],
                "FALLBACK"
            )
        ]

    def _build_mixed_fallback(
        self,
        location: str,
        latitude: Optional[float],
        longitude: Optional[float]
    ) -> List[Dict[str, Any]]:
        examples = self._get_local_examples(location, latitude, longitude)

        return [
            self._create_plan(
                "Paseo urbano con alternativa cubierta",
                "MIXTO",
                "Plan flexible para combinar exterior suave con una alternativa cercana si cambia el tiempo.",
                examples["ruta"]["placeName"],
                examples["ruta"]["address"],
                examples["ruta"]["query"],
                "FALLBACK"
            ),
            self._create_plan(
                "Café o zona de bares",
                "OCIO",
                "Alternativa sencilla si el viento, la lluvia o la incertidumbre aumentan.",
                examples["bar"]["placeName"],
                examples["bar"]["address"],
                examples["bar"]["query"],
                "FALLBACK"
            ),
            self._create_plan(
                "Escape room o juego de interior",
                "INTERIOR",
                "Plan cubierto de respaldo si no conviene estar al aire libre.",
                examples["escape"]["placeName"],
                examples["escape"]["address"],
                examples["escape"]["query"],
                "FALLBACK"
            ),
            self._create_plan(
                "Sitio de interés cercano",
                "MIXTO",
                "Plan adaptable para visitar algo cercano sin comprometerse con una ruta larga.",
                examples["interes"]["placeName"],
                examples["interes"]["address"],
                examples["interes"]["query"],
                "FALLBACK"
            ),
            self._create_plan(
                "Restaurante en zona recomendada",
                "RESTAURACION",
                "Opción estable para cerrar el plan aunque el tiempo cambie.",
                examples["restaurante"]["placeName"],
                examples["restaurante"]["address"],
                examples["restaurante"]["query"],
                "FALLBACK"
            )
        ]

    def _get_beach_plan_if_suitable(
        self,
        location: str,
        latitude: Optional[float],
        longitude: Optional[float],
        temperature: Optional[float],
        rain: Optional[int],
        wind: Optional[float],
        status: Optional[str],
        bad_weather: bool
    ) -> Optional[Dict[str, Any]]:
        if bad_weather:
            return None

        if not self._is_summer_or_hot(temperature):
            return None

        if not self._has_beach_nearby(location, latitude, longitude):
            return None

        normalized_status = (status or "").lower()

        if (
            "lluvia" in normalized_status
            or "llovizna" in normalized_status
            or "tormenta" in normalized_status
            or "storm" in normalized_status
            or "rain" in normalized_status
        ):
            return None

        if rain is not None and rain >= 40:
            return None

        if wind is not None and wind >= 30:
            return None

        if temperature is not None and temperature < 22:
            return None

        examples = self._get_local_examples(location, latitude, longitude)

        return self._create_plan(
            title="Playa y baño",
            category="EXTERIOR",
            description="Plan muy recomendable en verano si hace calor y no hay lluvia ni viento fuerte. Ideal para bañarse, pasear junto al agua o pasar la tarde al aire libre.",
            place_name=examples["playa"]["placeName"],
            address=examples["playa"]["address"],
            query=examples["playa"]["query"],
            source="FALLBACK"
        )

    def _get_non_repeated_covered_plan(
        self,
        location: str,
        latitude: Optional[float],
        longitude: Optional[float],
        current_plans: List[Dict[str, Any]]
    ) -> Optional[Dict[str, Any]]:
        examples = self._get_local_examples(location, latitude, longitude)

        covered_alternatives = [
            self._create_plan(
                title="Escape room",
                category="INTERIOR",
                description="Alternativa cubierta y más activa que el cine. Recomendado si la previsión no es totalmente fiable.",
                place_name=examples["escape"]["placeName"],
                address=examples["escape"]["address"],
                query=examples["escape"]["query"],
                source="FALLBACK"
            ),
            self._create_plan(
                title="Experiencia de realidad virtual",
                category="INTERIOR",
                description="Plan cubierto de ocio tecnológico, útil si hay lluvia, viento o incertidumbre meteorológica.",
                place_name=examples["realidad_virtual"]["placeName"],
                address=examples["realidad_virtual"]["address"],
                query=examples["realidad_virtual"]["query"],
                source="FALLBACK"
            ),
            self._create_plan(
                title="Recreativos o zona de ocio interior",
                category="OCIO",
                description="Plan cubierto informal para evitar depender del tiempo exterior.",
                place_name=examples["recreativos"]["placeName"],
                address=examples["recreativos"]["address"],
                query=examples["recreativos"]["query"],
                source="FALLBACK"
            ),
            self._create_plan(
                title="Bolera o juegos de interior",
                category="OCIO",
                description="Alternativa cubierta para un plan social sin depender de la lluvia o el viento.",
                place_name=examples["bolera"]["placeName"],
                address=examples["bolera"]["address"],
                query=examples["bolera"]["query"],
                source="FALLBACK"
            ),
            self._create_plan(
                title="Centro comercial con ocio",
                category="INTERIOR",
                description="Plan cubierto con varias opciones: tiendas, restauración, cine, recreativos o actividades de ocio.",
                place_name=examples["centro_comercial"]["placeName"],
                address=examples["centro_comercial"]["address"],
                query=examples["centro_comercial"]["query"],
                source="FALLBACK"
            )
        ]

        used_keys = set()

        for plan in current_plans:
            used_keys.add(self._safe_string(plan.get("title")).lower())
            used_keys.add(self._safe_string(plan.get("placeName")).lower())

        for plan in covered_alternatives:
            title_key = self._safe_string(plan.get("title")).lower()
            place_key = self._safe_string(plan.get("placeName")).lower()

            if title_key not in used_keys and place_key not in used_keys:
                return plan

        return None

    def _get_local_examples(
        self,
        location: str,
        latitude: Optional[float],
        longitude: Optional[float]
    ) -> Dict[str, Dict[str, str]]:
        normalized_location = location.lower()

        if (
            "barakaldo" in normalized_location
            or "baracaldo" in normalized_location
            or self._is_barakaldo_coordinates(latitude, longitude)
        ):
            return {
                "cine": {
                    "placeName": "Cinesa Max Ocio",
                    "address": "Max Ocio, Barakaldo",
                    "query": "cartelera Cinesa Max Ocio Barakaldo"
                },
                "eventos": {
                    "placeName": "BEC Bilbao Exhibition Centre",
                    "address": "Ronda de Azkue, Barakaldo",
                    "query": "agenda BEC Bilbao Exhibition Centre Barakaldo"
                },
                "cultura": {
                    "placeName": "Azkuna Zentroa o centros culturales cercanos",
                    "address": "Bilbao y Barakaldo",
                    "query": "centros culturales exposiciones Barakaldo Bilbao"
                },
                "bar": {
                    "placeName": "Zona de Herriko Plaza",
                    "address": "Herriko Plaza, Barakaldo",
                    "query": "bares cafeterías Herriko Plaza Barakaldo"
                },
                "restaurante": {
                    "placeName": "Zona de Herriko Plaza",
                    "address": "Herriko Plaza, Barakaldo",
                    "query": "restaurantes Herriko Plaza Barakaldo"
                },
                "ruta": {
                    "placeName": "Paseo por la ría de Barakaldo",
                    "address": "Barakaldo",
                    "query": "paseo ría Barakaldo"
                },
                "parque": {
                    "placeName": "Jardín Botánico Ramón Rubial",
                    "address": "Barakaldo",
                    "query": "Jardín Botánico Ramón Rubial Barakaldo"
                },
                "interes": {
                    "placeName": "Puente Bizkaia y margen izquierda",
                    "address": "Portugalete y Getxo",
                    "query": "Puente Bizkaia visita Portugalete Getxo"
                },
                "escape": {
                    "placeName": "Escape rooms de Barakaldo o Bilbao",
                    "address": "Barakaldo y Bilbao",
                    "query": "escape room Barakaldo Bilbao"
                },
                "realidad_virtual": {
                    "placeName": "Experiencias de realidad virtual en Bilbao o Barakaldo",
                    "address": "Bilbao y Barakaldo",
                    "query": "realidad virtual inmersiva Bilbao Barakaldo"
                },
                "recreativos": {
                    "placeName": "Zona de ocio de Max Center o Max Ocio",
                    "address": "Barakaldo",
                    "query": "recreativos ocio interior Max Center Max Ocio Barakaldo"
                },
                "bolera": {
                    "placeName": "Bolera o juegos de interior en Barakaldo",
                    "address": "Barakaldo",
                    "query": "bolera juegos interior Barakaldo"
                },
                "centro_comercial": {
                    "placeName": "Max Center y Max Ocio",
                    "address": "Barakaldo",
                    "query": "Max Center Max Ocio Barakaldo ocio"
                },
                "playa": {
                    "placeName": "Playa de Ereaga o Playa de Sopela",
                    "address": "Getxo y Sopela",
                    "query": "Playa de Ereaga Sopela desde Barakaldo estado bandera"
                }
            }

        if "bilbao" in normalized_location or self._is_bilbao_coordinates(latitude, longitude):
            return {
                "cine": {
                    "placeName": "Cines Zubiarte",
                    "address": "Centro Comercial Zubiarte, Bilbao",
                    "query": "cartelera Cines Zubiarte Bilbao"
                },
                "eventos": {
                    "placeName": "Azkuna Zentroa",
                    "address": "Bilbao",
                    "query": "agenda Azkuna Zentroa Bilbao"
                },
                "cultura": {
                    "placeName": "Museo Guggenheim Bilbao",
                    "address": "Abandoibarra Etorbidea, Bilbao",
                    "query": "Museo Guggenheim Bilbao horarios entradas"
                },
                "bar": {
                    "placeName": "Zona de Plaza Nueva",
                    "address": "Casco Viejo, Bilbao",
                    "query": "bares Plaza Nueva Bilbao"
                },
                "restaurante": {
                    "placeName": "Zona del Casco Viejo",
                    "address": "Casco Viejo, Bilbao",
                    "query": "restaurantes Casco Viejo Bilbao"
                },
                "ruta": {
                    "placeName": "Paseo por Abandoibarra",
                    "address": "Bilbao",
                    "query": "paseo Abandoibarra Bilbao"
                },
                "parque": {
                    "placeName": "Parque Doña Casilda",
                    "address": "Bilbao",
                    "query": "Parque Doña Casilda Bilbao"
                },
                "interes": {
                    "placeName": "Mirador de Artxanda",
                    "address": "Bilbao",
                    "query": "Mirador de Artxanda Bilbao"
                },
                "escape": {
                    "placeName": "Escape rooms de Bilbao",
                    "address": "Bilbao",
                    "query": "escape room Bilbao"
                },
                "realidad_virtual": {
                    "placeName": "Experiencias de realidad virtual en Bilbao",
                    "address": "Bilbao",
                    "query": "realidad virtual inmersiva Bilbao"
                },
                "recreativos": {
                    "placeName": "Recreativos o salones arcade en Bilbao",
                    "address": "Bilbao",
                    "query": "recreativos arcade Bilbao"
                },
                "bolera": {
                    "placeName": "Bolera o juegos de interior en Bilbao",
                    "address": "Bilbao",
                    "query": "bolera juegos interior Bilbao"
                },
                "centro_comercial": {
                    "placeName": "Centro Comercial Zubiarte",
                    "address": "Bilbao",
                    "query": "Centro Comercial Zubiarte Bilbao ocio"
                },
                "playa": {
                    "placeName": "Playa de Ereaga, Arrigunaga o Sopela",
                    "address": "Getxo y Sopela",
                    "query": "playas cerca de Bilbao Ereaga Arrigunaga Sopela estado bandera"
                }
            }

        return {
            "cine": {
                "placeName": "Cine de referencia en la zona",
                "address": location,
                "query": f"cartelera cine cerca de {location}"
            },
            "eventos": {
                "placeName": "Agenda cultural cercana",
                "address": location,
                "query": f"eventos exposiciones hoy cerca de {location}"
            },
            "cultura": {
                "placeName": "Museo o centro cultural de la zona",
                "address": location,
                "query": f"museo centro cultural cerca de {location}"
            },
            "bar": {
                "placeName": "Zona de bares del centro",
                "address": location,
                "query": f"bares cafeterías centro {location}"
            },
            "restaurante": {
                "placeName": "Zona de restaurantes del centro",
                "address": location,
                "query": f"restaurantes centro {location}"
            },
            "ruta": {
                "placeName": "Paseo urbano principal",
                "address": location,
                "query": f"ruta urbana paseo cerca de {location}"
            },
            "parque": {
                "placeName": "Parque o zona verde principal",
                "address": location,
                "query": f"parques zonas verdes cerca de {location}"
            },
            "interes": {
                "placeName": "Punto de interés destacado",
                "address": location,
                "query": f"sitios de interés cerca de {location}"
            },
            "escape": {
                "placeName": "Escape room cercano",
                "address": location,
                "query": f"escape room cerca de {location}"
            },
            "realidad_virtual": {
                "placeName": "Experiencia de realidad virtual cercana",
                "address": location,
                "query": f"realidad virtual inmersiva cerca de {location}"
            },
            "recreativos": {
                "placeName": "Recreativos o arcade cercano",
                "address": location,
                "query": f"recreativos arcade cerca de {location}"
            },
            "bolera": {
                "placeName": "Bolera o juegos de interior cercanos",
                "address": location,
                "query": f"bolera juegos interior cerca de {location}"
            },
            "centro_comercial": {
                "placeName": "Centro comercial con ocio cercano",
                "address": location,
                "query": f"centro comercial ocio cerca de {location}"
            },
            "playa": {
                "placeName": "Playa o zona de baño cercana",
                "address": location,
                "query": f"playa zona de baño cerca de {location}"
            }
        }

    def _add_bilbao_area_plans(
        self,
        plans: List[Dict[str, Any]],
        location: str,
        latitude: Optional[float],
        longitude: Optional[float]
    ) -> List[Dict[str, Any]]:
        local = location.lower()

        if (
            "barakaldo" in local
            or "baracaldo" in local
            or self._is_barakaldo_coordinates(latitude, longitude)
        ):
            plans.insert(
                0,
                self._create_plan(
                    "Consultar agenda del BEC",
                    "EVENTOS",
                    "Plan especialmente relevante para Barakaldo. Conviene revisar ferias, exposiciones o eventos activos.",
                    "BEC Bilbao Exhibition Centre",
                    "Ronda de Azkue, Barakaldo",
                    "agenda BEC Bilbao Exhibition Centre Barakaldo",
                    "FALLBACK_LOCAL"
                )
            )

            plans.insert(
                1,
                self._create_plan(
                    "Ocio interior en Max Center o Max Ocio",
                    "INTERIOR",
                    "Plan cubierto con opciones de cine, restauración, recreativos, tiendas y ocio interior.",
                    "Max Center y Max Ocio",
                    "Barakaldo",
                    "Max Center Max Ocio Barakaldo ocio recreativos",
                    "FALLBACK_LOCAL"
                )
            )

            return plans

        if "bilbao" in local or self._is_bilbao_coordinates(latitude, longitude):
            plans.insert(
                0,
                self._create_plan(
                    "Visita al Guggenheim",
                    "CULTURA",
                    "Plan cultural cubierto o mixto, adecuado especialmente con lluvia o viento.",
                    "Museo Guggenheim Bilbao",
                    "Abandoibarra Etorbidea, Bilbao",
                    "Museo Guggenheim Bilbao entradas horarios",
                    "FALLBACK_LOCAL"
                )
            )

            plans.insert(
                1,
                self._create_plan(
                    "Agenda de Azkuna Zentroa",
                    "CULTURA",
                    "Espacio cultural cubierto útil como alternativa si el tiempo cambia.",
                    "Azkuna Zentroa",
                    "Bilbao",
                    "Azkuna Zentroa agenda Bilbao",
                    "FALLBACK_LOCAL"
                )
            )

        return plans

    def _create_plan(
        self,
        title: str,
        category: str,
        description: str,
        place_name: str,
        address: str,
        query: str,
        source: str
    ) -> Dict[str, Any]:
        return {
            "title": title,
            "category": category,
            "description": description,
            "placeName": place_name,
            "address": address,
            "externalUrl": self._build_google_search_url(query),
            "source": source
        }

    def _build_google_search_url(self, query: str) -> str:
        return f"https://www.google.com/search?q={quote_plus(query)}"

    def _is_summer_or_hot(self, temperature: Optional[float]) -> bool:
        current_month = date.today().month

        if current_month in [6, 7, 8, 9]:
            return True

        return temperature is not None and temperature >= 26

    def _has_beach_nearby(
        self,
        location: str,
        latitude: Optional[float],
        longitude: Optional[float]
    ) -> bool:
        normalized_location = location.lower()

        coastal_keywords = [
            "bilbao",
            "barakaldo",
            "baracaldo",
            "getxo",
            "sopela",
            "sopelana",
            "portugalete",
            "santurtzi",
            "santander",
            "barcelona",
            "valencia"
        ]

        if any(keyword in normalized_location for keyword in coastal_keywords):
            return True

        if latitude is None or longitude is None:
            return False

        is_bilbao_area = 43.18 <= latitude <= 43.37 and -3.12 <= longitude <= -2.75
        is_santander_area = 43.38 <= latitude <= 43.52 and -3.95 <= longitude <= -3.70
        is_barcelona_area = 41.30 <= latitude <= 41.50 and 2.05 <= longitude <= 2.25
        is_valencia_area = 39.35 <= latitude <= 39.55 and -0.45 <= longitude <= -0.25

        return (
            is_bilbao_area
            or is_santander_area
            or is_barcelona_area
            or is_valencia_area
        )

    def _is_bilbao_area(
        self,
        location_name: str,
        latitude: Optional[float],
        longitude: Optional[float]
    ) -> bool:
        location = location_name.lower()

        return (
            "bilbao" in location
            or "barakaldo" in location
            or "baracaldo" in location
            or "getxo" in location
            or "sestao" in location
            or "portugalete" in location
            or "leioa" in location
            or self._is_bilbao_coordinates(latitude, longitude)
            or self._is_barakaldo_coordinates(latitude, longitude)
        )

    def _is_bilbao_coordinates(
        self,
        latitude: Optional[float],
        longitude: Optional[float]
    ) -> bool:
        if latitude is None or longitude is None:
            return False

        return 43.18 <= latitude <= 43.37 and -3.12 <= longitude <= -2.75

    def _is_barakaldo_coordinates(
        self,
        latitude: Optional[float],
        longitude: Optional[float]
    ) -> bool:
        if latitude is None or longitude is None:
            return False

        return 43.26 <= latitude <= 43.33 and -3.06 <= longitude <= -2.94

    def _merge_plans(
        self,
        ai_plans: List[Dict[str, Any]],
        fallback_plans: List[Dict[str, Any]]
    ) -> List[Dict[str, Any]]:
        return self._remove_duplicates(ai_plans + fallback_plans)[:8]

    def _remove_duplicates(self, plans: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        result = []
        seen = set()

        for plan in plans:
            title = self._safe_string(plan.get("title")).lower()
            place_name = self._safe_string(plan.get("placeName")).lower()
            category = self._safe_string(plan.get("category")).lower()

            key = (title, place_name)

            if key in seen:
                continue

            if self._is_similar_plan_already_added(result, title, place_name, category):
                continue

            seen.add(key)
            result.append(plan)

        return result

    def _is_similar_plan_already_added(
        self,
        current_plans: List[Dict[str, Any]],
        title: str,
        place_name: str,
        category: str
    ) -> bool:
        for plan in current_plans:
            existing_title = self._safe_string(plan.get("title")).lower()
            existing_place = self._safe_string(plan.get("placeName")).lower()
            existing_category = self._safe_string(plan.get("category")).lower()

            if place_name and existing_place and place_name == existing_place:
                return True

            if category == "cultura" and existing_category == "cultura":
                return True

            if "cine" in title and "cine" in existing_title:
                return True

            if "restaurante" in title and "restaurante" in existing_title:
                return True

            if "bar" in title and "bar" in existing_title:
                return True

            if "escape" in title and "escape" in existing_title:
                return True

        return False

    def _is_too_generic_plan(self, title: str, place_name: str) -> bool:
        normalized_title = title.lower()
        normalized_place = place_name.lower()

        generic_titles = [
            "buscar",
            "buscar en google",
            "consultar en google"
        ]

        generic_places = [
            "",
            "bar cercano",
            "bares cercanos",
            "restaurante cercano",
            "restaurantes cercanos",
            "cine cercano",
            "actividad cercana",
            "sitio cercano",
            "lugar cercano",
            "evento cercano",
            "eventos cercanos"
        ]

        if any(item in normalized_title for item in generic_titles):
            return True

        if normalized_place in generic_places:
            return True

        return False

    def _safe_string(self, value: Any) -> str:
        if value is None:
            return ""

        return str(value).strip()