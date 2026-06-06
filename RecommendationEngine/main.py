from fastapi import FastAPI
from app.models import WeatherPlanRecommendationRequest, WeatherPlanRecommendationResponse
from app.recommendation_service import RecommendationService

app = FastAPI(
    title="Weather&Go Recommendation Engine",
    description="Motor inteligente de recomendaciones basado en datos meteorológicos agregados.",
    version="1.0.0"
)

recommendation_service = RecommendationService()


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