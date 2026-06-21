import json
import os
from urllib.parse import quote_plus

from google import genai
from google.genai import types


class AiPlanService:

    def get_ai_plans(self, location_name, category, weather_summary, latitude=None, longitude=None):
        api_key = os.getenv("GEMINI_API_KEY")

        if not api_key:
            return self.get_fallback_plans(location_name, category)

        try:
            client = genai.Client(api_key=api_key)

            grounding_tool = types.Tool(
                google_search=types.GoogleSearch()
            )

            prompt = f"""
Devuelve únicamente JSON válido. No expliques nada.

Ubicación: {location_name}
Latitud: {latitude}
Longitud: {longitude}
Categoría recomendada: {category}
Tiempo actual: {weather_summary}

Necesito entre 5 y 8 planes personalizados cerca de la ubicación.

Ten en cuenta:
- Si hace mal tiempo, prioriza planes de interior.
- Si hace buen tiempo, incluye planes al aire libre.
- Si la ubicación es Barakaldo o alrededores, ten en cuenta el BEC.
- Incluye eventos, cine, bares, restaurantes, sitios de interés, museos, exposiciones, rutas o centros comerciales según corresponda.
- No confirmes eventos concretos si no estás seguro. En ese caso usa una descripción de consulta.

Estructura obligatoria:

{{
  "plans": [
    {{
      "title": "string",
      "description": "string",
      "type": "EVENT | CINEMA | BAR | RESTAURANT | ROUTE | MUSEUM | SHOPPING | POINT_OF_INTEREST",
      "category": "INTERIOR | EXTERIOR | MIXED",
      "url": "string",
      "source": "AI_SEARCH"
    }}
  ]
}}
"""

            response = client.models.generate_content(
                model="gemini-2.5-flash",
                contents=prompt,
                config=types.GenerateContentConfig(
                    tools=[grounding_tool]
                )
            )

            text = response.text.strip()
            text = text.replace("```json", "").replace("```", "").strip()

            data = json.loads(text)

            if "plans" not in data or not isinstance(data["plans"], list):
                return self.get_fallback_plans(location_name, category)

            return data["plans"]

        except Exception:
            return self.get_fallback_plans(location_name, category)

    def get_fallback_plans(self, location_name, category):
        return [
            self.create_plan(
                "Eventos cercanos",
                f"Busca eventos, exposiciones o actividades cerca de {location_name}.",
                "EVENT",
                "INTERIOR",
                f"eventos cerca de {location_name}"
            ),
            self.create_plan(
                "Cines cercanos",
                "Consulta la cartelera de cines cercanos.",
                "CINEMA",
                "INTERIOR",
                f"cines cerca de {location_name}"
            ),
            self.create_plan(
                "Bares cercanos",
                "Plan flexible recomendado según el tiempo actual.",
                "BAR",
                "INTERIOR",
                f"bares cerca de {location_name}"
            ),
            self.create_plan(
                "Restaurantes cercanos",
                "Opción recomendable para comer o cenar cerca de la zona.",
                "RESTAURANT",
                "INTERIOR",
                f"restaurantes cerca de {location_name}"
            ),
            self.create_plan(
                "Sitios de interés cercanos",
                "Consulta lugares interesantes para visitar cerca de la ubicación.",
                "POINT_OF_INTEREST",
                "MIXED",
                f"sitios de interés cerca de {location_name}"
            )
        ]

    def create_plan(self, title, description, plan_type, category, query):
        encoded_query = quote_plus(query)

        return {
            "title": title,
            "description": description,
            "type": plan_type,
            "category": category,
            "url": f"https://www.google.com/maps/search/{encoded_query}",
            "source": "FALLBACK"
        }