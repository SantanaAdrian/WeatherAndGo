from typing import List, Optional
from pydantic import BaseModel


class ProviderWeatherData(BaseModel):
    provider: str
    temperature: Optional[float] = None
    humidity: Optional[int] = None
    windSpeed: Optional[float] = None
    precipitationProbability: Optional[int] = None
    weatherStatus: Optional[str] = None


class AggregationSummary(BaseModel):
    providerCount: int
    temperatureDifference: Optional[float] = None
    humidityDifference: Optional[int] = None
    windSpeedDifference: Optional[float] = None
    precipitationDifference: Optional[int] = None
    reliabilityLevel: str
    explanation: Optional[str] = None


class WeatherPlanRecommendationRequest(BaseModel):
    locationName: str
    currentTemperature: float
    currentHumidity: int
    currentWindSpeed: float
    currentPrecipitationProbability: int
    currentWeatherStatus: str
    aggregationSummary: AggregationSummary
    providerData: List[ProviderWeatherData]


class WeatherPlanRecommendationResponse(BaseModel):
    category: str
    confidence: int
    summary: str
    recommendedPlanTypes: List[str]
    reasons: List[str]