import math
from typing import Any, Dict, List, Optional

import requests


class PlacesService:

    OVERPASS_URL = "https://overpass-api.de/api/interpreter"

    def find_places_for_category(
        self,
        category: str,
        latitude: float,
        longitude: float,
        location_name: str
    ) -> List[Dict[str, Any]]:
        normalized_category = self._normalize_category(category)
        osm_filters = self._get_osm_filters(normalized_category)
        radius = self._get_radius(normalized_category)

        places = []

        for place_type, filters in osm_filters.items():
            query = self._build_overpass_query(latitude, longitude, radius, filters)

            try:
                response = requests.post(
                    self.OVERPASS_URL,
                    data={"data": query},
                    timeout=12
                )

                response.raise_for_status()
                data = response.json()

                places.extend(
                    self._map_overpass_elements(
                        data=data,
                        place_type=place_type,
                        latitude=latitude,
                        longitude=longitude,
                        category=normalized_category
                    )
                )

            except Exception:
                continue

        unique_places = self._remove_duplicates(places)
        sorted_places = sorted(
            unique_places,
            key=lambda item: item.get("distanceMeters") or 999999
        )

        if not sorted_places:
            return self._build_fallback_places(normalized_category, location_name)

        return sorted_places[:6]

    def _normalize_category(self, category: str) -> str:
        if not category:
            return "MIXTO"

        value = category.upper().strip()

        if value not in ["INTERIOR", "EXTERIOR", "MIXTO", "PRECAUCION"]:
            return "MIXTO"

        return value

    def _get_radius(self, category: str) -> int:
        if category == "EXTERIOR":
            return 5000

        if category == "PRECAUCION":
            return 2500

        return 3500

    def _get_osm_filters(self, category: str) -> Dict[str, List[str]]:
        if category == "EXTERIOR":
            return {
                "PARQUE": [
                    '["leisure"="park"]',
                    '["leisure"="garden"]'
                ],
                "MIRADOR": [
                    '["tourism"="viewpoint"]'
                ],
                "RUTA_SUAVE": [
                    '["highway"="path"]',
                    '["highway"="footway"]'
                ]
            }

        if category == "INTERIOR":
            return {
                "CINE": [
                    '["amenity"="cinema"]'
                ],
                "MUSEO": [
                    '["tourism"="museum"]'
                ],
                "CAFETERIA": [
                    '["amenity"="cafe"]'
                ],
                "BIBLIOTECA": [
                    '["amenity"="library"]'
                ]
            }

        if category == "PRECAUCION":
            return {
                "CINE": [
                    '["amenity"="cinema"]'
                ],
                "CAFETERIA": [
                    '["amenity"="cafe"]'
                ],
                "MUSEO": [
                    '["tourism"="museum"]'
                ],
                "CENTRO_CULTURAL": [
                    '["amenity"="arts_centre"]',
                    '["amenity"="community_centre"]'
                ]
            }

        return {
            "CAFETERIA": [
                '["amenity"="cafe"]'
            ],
            "BAR": [
                '["amenity"="bar"]'
            ],
            "PARQUE": [
                '["leisure"="park"]'
            ],
            "CINE": [
                '["amenity"="cinema"]'
            ],
            "MUSEO": [
                '["tourism"="museum"]'
            ]
        }

    def _build_overpass_query(
        self,
        latitude: float,
        longitude: float,
        radius: int,
        filters: List[str]
    ) -> str:
        query_parts = []

        for osm_filter in filters:
            query_parts.append(f'node(around:{radius},{latitude},{longitude}){osm_filter};')
            query_parts.append(f'way(around:{radius},{latitude},{longitude}){osm_filter};')
            query_parts.append(f'relation(around:{radius},{latitude},{longitude}){osm_filter};')

        joined_query_parts = "\n".join(query_parts)

        return f"""
        [out:json][timeout:12];
        (
          {joined_query_parts}
        );
        out center tags 20;
        """

    def _map_overpass_elements(
        self,
        data: Dict[str, Any],
        place_type: str,
        latitude: float,
        longitude: float,
        category: str
    ) -> List[Dict[str, Any]]:
        elements = data.get("elements", [])
        places = []

        for element in elements:
            tags = element.get("tags", {})
            name = tags.get("name")

            if not name:
                continue

            place_latitude = self._get_element_latitude(element)
            place_longitude = self._get_element_longitude(element)

            if place_latitude is None or place_longitude is None:
                continue

            distance = self._calculate_distance_meters(
                latitude,
                longitude,
                place_latitude,
                place_longitude
            )

            places.append({
                "title": name,
                "type": place_type,
                "description": self._build_description(place_type, category),
                "source": "OPENSTREETMAP",
                "url": self._build_openstreetmap_url(element),
                "latitude": place_latitude,
                "longitude": place_longitude,
                "distanceMeters": round(distance)
            })

        return places

    def _get_element_latitude(self, element: Dict[str, Any]) -> Optional[float]:
        if "lat" in element:
            return element.get("lat")

        center = element.get("center")

        if center:
            return center.get("lat")

        return None

    def _get_element_longitude(self, element: Dict[str, Any]) -> Optional[float]:
        if "lon" in element:
            return element.get("lon")

        center = element.get("center")

        if center:
            return center.get("lon")

        return None

    def _build_openstreetmap_url(self, element: Dict[str, Any]) -> str:
        element_type = element.get("type")
        element_id = element.get("id")

        return f"https://www.openstreetmap.org/{element_type}/{element_id}"

    def _build_description(self, place_type: str, category: str) -> str:
        if category == "EXTERIOR":
            return "Plan exterior recomendado por las condiciones meteorológicas actuales."

        if category == "INTERIOR":
            return "Plan de interior recomendado por las condiciones meteorológicas actuales."

        if category == "PRECAUCION":
            return "Plan cubierto o cercano recomendado por condiciones meteorológicas sensibles."

        if place_type in ["CAFETERIA", "BAR", "CINE", "MUSEO"]:
            return "Alternativa cubierta recomendada para mantener flexibilidad."

        return "Plan compatible con una recomendación mixta."

    def _calculate_distance_meters(
        self,
        latitude_1: float,
        longitude_1: float,
        latitude_2: float,
        longitude_2: float
    ) -> float:
        earth_radius = 6371000

        lat_1 = math.radians(latitude_1)
        lat_2 = math.radians(latitude_2)
        delta_lat = math.radians(latitude_2 - latitude_1)
        delta_lon = math.radians(longitude_2 - longitude_1)

        a = (
            math.sin(delta_lat / 2) ** 2
            + math.cos(lat_1)
            * math.cos(lat_2)
            * math.sin(delta_lon / 2) ** 2
        )

        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))

        return earth_radius * c

    def _remove_duplicates(self, places: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        seen_names = set()
        unique_places = []

        for place in places:
            key = place.get("title", "").lower().strip()

            if not key or key in seen_names:
                continue

            seen_names.add(key)
            unique_places.append(place)

        return unique_places

    def _build_fallback_places(
        self,
        category: str,
        location_name: str
    ) -> List[Dict[str, Any]]:
        if category == "EXTERIOR":
            return [
                {
                    "title": f"Buscar ruta suave cerca de {location_name}",
                    "type": "RUTA_SUAVE",
                    "description": "No se han encontrado lugares concretos en OpenStreetMap, pero el clima permite valorar un paseo o ruta sencilla.",
                    "source": "WEATHER_AND_GO",
                    "url": None,
                    "latitude": None,
                    "longitude": None,
                    "distanceMeters": None
                }
            ]

        if category == "INTERIOR":
            return [
                {
                    "title": f"Buscar plan cubierto cerca de {location_name}",
                    "type": "INTERIOR",
                    "description": "No se han encontrado lugares concretos en OpenStreetMap, pero se recomienda priorizar cine, museo, cafetería o centro cultural.",
                    "source": "WEATHER_AND_GO",
                    "url": None,
                    "latitude": None,
                    "longitude": None,
                    "distanceMeters": None
                }
            ]

        return [
            {
                "title": f"Plan flexible cerca de {location_name}",
                "type": "MIXTO",
                "description": "No se han encontrado lugares concretos en OpenStreetMap, pero se recomienda combinar un plan urbano corto con una alternativa cubierta.",
                "source": "WEATHER_AND_GO",
                "url": None,
                "latitude": None,
                "longitude": None,
                "distanceMeters": None
            }
        ]