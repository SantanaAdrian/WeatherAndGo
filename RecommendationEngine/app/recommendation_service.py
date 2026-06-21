from app.models import WeatherPlanRecommendationRequest, WeatherPlanRecommendationResponse
from app.ai_plan_service import AiPlanService


class RecommendationService:

    def __init__(self):
        self.ai_plan_service = AiPlanService()

    def generate_recommendation(
        self,
        request: WeatherPlanRecommendationRequest
    ) -> WeatherPlanRecommendationResponse:
        status = self._normalize(request.currentWeatherStatus)

        interior_score = 0
        exterior_score = 0
        mixed_score = 0
        caution_score = 0
        reasons = []

        temperature = request.currentTemperature
        rain = request.currentPrecipitationProbability
        wind = request.currentWindSpeed
        reliability = request.aggregationSummary.reliabilityLevel.upper()

        if self._contains_storm(status):
            caution_score += 80
            interior_score += 40
            reasons.append("Posible tormenta detectada en el estado meteorológico.")

        if self._contains_rain(status):
            interior_score += 45
            mixed_score += 25
            reasons.append("El estado meteorológico indica lluvia o llovizna.")

        if rain >= 70:
            interior_score += 60
            caution_score += 25
            reasons.append("La probabilidad de lluvia es alta.")
        elif rain >= 40:
            interior_score += 35
            mixed_score += 35
            reasons.append("Hay probabilidad moderada de lluvia.")
        elif rain >= 20:
            mixed_score += 30
            reasons.append("Existe una probabilidad baja o moderada de lluvia.")
        else:
            exterior_score += 30
            reasons.append("La probabilidad de lluvia es baja.")

        if wind >= 45:
            caution_score += 60
            interior_score += 40
            reasons.append("El viento es fuerte.")
        elif wind >= 30:
            interior_score += 30
            mixed_score += 35
            caution_score += 15
            reasons.append("El viento es moderado-alto.")
        elif wind >= 20:
            mixed_score += 40
            interior_score += 10
            reasons.append("El viento es moderado y puede afectar a planes exteriores prolongados.")
        else:
            exterior_score += 25
            reasons.append("El viento es bajo o moderado.")

        if temperature >= 34:
            caution_score += 35
            interior_score += 35
            reasons.append("La temperatura es elevada.")
        elif temperature >= 29:
            mixed_score += 25
            interior_score += 15
            reasons.append("La temperatura es alta, conviene evitar esfuerzos intensos.")
        elif 15 <= temperature <= 26:
            exterior_score += 35
            reasons.append("La temperatura es adecuada para actividades al aire libre.")
        elif temperature <= 6:
            interior_score += 35
            mixed_score += 10
            reasons.append("La temperatura es baja.")
        else:
            mixed_score += 20
            reasons.append("La temperatura es moderada.")

        if reliability == "ALTA":
            exterior_score += 10
            mixed_score += 5
            reasons.append("La fiabilidad de la predicción agregada es alta.")
        elif reliability == "MEDIA":
            mixed_score += 20
            reasons.append("La fiabilidad de la predicción agregada es media.")
        else:
            caution_score += 25
            mixed_score += 20
            reasons.append("La fiabilidad de la predicción agregada es baja.")

        category = self._select_category(
            interior_score,
            exterior_score,
            mixed_score,
            caution_score
        )

        if category == "EXTERIOR" and wind >= 20:
            category = "MIXTO"

        if category == "EXTERIOR" and reliability != "ALTA":
            category = "MIXTO"

        confidence = self._calculate_confidence(
            category,
            interior_score,
            exterior_score,
            mixed_score,
            caution_score,
            reliability,
            wind
        )

        summary = self._build_summary(
            category,
            request.locationName,
            temperature,
            rain,
            wind,
            reliability
        )

        weather_summary = self._build_weather_summary(request)

        plans = self.ai_plan_service.get_ai_plans(
            location_name=request.locationName,
            category=category,
            weather_summary=weather_summary,
            latitude=getattr(request, "latitude", None),
            longitude=getattr(request, "longitude", None),
            temperature=temperature,
            rain=rain,
            wind=wind,
            reliability=reliability,
            status=status
        )

        return WeatherPlanRecommendationResponse(
            category=category,
            confidence=confidence,
            summary=summary,
            recommendedPlanTypes=self._get_plan_types(category),
            reasons=reasons,
            personalizedPlans=plans
        )

    def _select_category(
        self,
        interior_score: int,
        exterior_score: int,
        mixed_score: int,
        caution_score: int
    ) -> str:
        scores = {
            "INTERIOR": interior_score,
            "EXTERIOR": exterior_score,
            "MIXTO": mixed_score,
            "PRECAUCION": caution_score
        }

        return max(scores, key=scores.get)

    def _calculate_confidence(
        self,
        category: str,
        interior_score: int,
        exterior_score: int,
        mixed_score: int,
        caution_score: int,
        reliability: str,
        wind: float
    ) -> int:
        scores = [interior_score, exterior_score, mixed_score, caution_score]
        top_score = max(scores)
        second_score = sorted(scores, reverse=True)[1]

        confidence = 60 + min(top_score - second_score, 25)

        if reliability == "ALTA":
            confidence += 10
        elif reliability == "MEDIA":
            confidence -= 5
        elif reliability == "BAJA":
            confidence -= 12

        if category == "MIXTO":
            confidence -= 3

        if wind >= 20:
            confidence -= 5

        return max(40, min(confidence, 95))

    def _build_summary(
        self,
        category: str,
        location_name: str,
        temperature: float,
        rain: int,
        wind: float,
        reliability: str
    ) -> str:
        if category == "INTERIOR":
            return (
                f"Para {location_name}, las condiciones favorecen planes de interior. "
                f"La previsión indica {temperature:.1f}°C, {rain}% de lluvia y viento de {wind:.1f} km/h."
            )

        if category == "EXTERIOR":
            return (
                f"Para {location_name}, las condiciones son adecuadas para planes al aire libre. "
                f"La previsión agregada es estable, con {temperature:.1f}°C y baja probabilidad de lluvia."
            )

        if category == "PRECAUCION":
            return (
                f"Para {location_name}, conviene actuar con precaución y evitar planes exteriores exigentes. "
                f"Hay factores meteorológicos que pueden afectar al desarrollo de actividades al aire libre."
            )

        return (
            f"Para {location_name}, se recomienda un plan mixto con alternativa cubierta. "
            f"La temperatura es agradable y la lluvia es baja, pero el viento y la fiabilidad {reliability.lower()} aconsejan flexibilidad."
        )

    def _build_weather_summary(
        self,
        request: WeatherPlanRecommendationRequest
    ) -> str:
        return (
            f"Ubicación: {request.locationName}. "
            f"Temperatura actual: {request.currentTemperature:.1f}°C. "
            f"Humedad actual: {request.currentHumidity}%. "
            f"Viento actual: {request.currentWindSpeed:.1f} km/h. "
            f"Probabilidad de precipitación: {request.currentPrecipitationProbability}%. "
            f"Estado meteorológico: {request.currentWeatherStatus}. "
            f"Fiabilidad agregada: {request.aggregationSummary.reliabilityLevel}."
        )

    def _get_plan_types(
        self,
        category: str
    ) -> list[str]:
        if category == "INTERIOR":
            return [
                "cine",
                "museo",
                "cafetería",
                "centro comercial",
                "actividad cultural cubierta",
                "eventos cercanos",
                "bares",
                "restaurantes"
            ]

        if category == "EXTERIOR":
            return [
                "paseo urbano",
                "ruta suave",
                "parque",
                "mirador",
                "sitios de interés",
                "terraza",
                "actividad deportiva ligera"
            ]

        if category == "PRECAUCION":
            return [
                "plan de interior",
                "actividad cercana",
                "eventos cubiertos",
                "cine",
                "cafetería",
                "evitar zonas expuestas",
                "revisar la previsión antes de salir"
            ]

        return [
            "paseo corto",
            "cafetería",
            "plan urbano",
            "actividad cubierta alternativa",
            "eventos cercanos",
            "bares",
            "restaurantes",
            "ruta sencilla si el viento baja"
        ]

    def _normalize(self, value: str) -> str:
        if not value:
            return ""

        return value.lower()

    def _contains_rain(self, status: str) -> bool:
        return (
            "lluvia" in status
            or "llovizna" in status
            or "chubasco" in status
            or "rain" in status
            or "drizzle" in status
            or "shower" in status
        )

    def _contains_storm(self, status: str) -> bool:
        return (
            "tormenta" in status
            or "storm" in status
            or "thunder" in status
        )