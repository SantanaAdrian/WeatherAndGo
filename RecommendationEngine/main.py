from fastapi import FastAPI
from app.models import WeatherPlanRecommendationRequest, WeatherPlanRecommendationResponse
from app.places_service import PlacesService
from app.recommendation_service import RecommendationService

app = FastAPI(
    title="Weather&Go Recommendation Engine",
    description="Motor inteligente de recomendaciones basado en datos meteorológicos agregados.",
    version="1.0.0"
)

recommendation_service = RecommendationService()
places_service = PlacesService()


@app.get("/health")
def health_check() -> dict:
    return {
        "status": "OK",
        "service": "Weather&Go Recommendation Engine"
    }


@app.post("/recommendations/weather-plan", response_model=WeatherPlanRecommendationResponse)
def generate_weather_plan_recommendation(
    request: WeatherPlanRecommendationRequest
) -> WeatherPlanRecommendationResponse:
    return recommendation_service.generate_recommendation(request)


@app.get("/places/test")
def test_places(
    category: str,
    latitude: float,
    longitude: float,
    locationName: str = "Ubicación seleccionada"
) -> dict:
    places = places_service.find_places_for_category(
        category=category,
        latitude=latitude,
        longitude=longitude,
        location_name=locationName
    )

    return {
        "category": category,
        "latitude": latitude,
        "longitude": longitude,
        "locationName": locationName,
        "places": places
    }