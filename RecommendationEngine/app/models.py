from typing import List, Optional

from pydantic import BaseModel, Field


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
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    currentTemperature: float
    currentHumidity: int
    currentWindSpeed: float
    currentPrecipitationProbability: int
    currentWeatherStatus: str
    aggregationSummary: AggregationSummary
    providerData: List[ProviderWeatherData]


class PlanRecommendation(BaseModel):
    title: str
    category: str
    description: str
    placeName: Optional[str] = None
    address: Optional[str] = None
    externalUrl: Optional[str] = None
    source: Optional[str] = None


class WeatherPlanRecommendationResponse(BaseModel):
    category: str
    confidence: int
    summary: str
    recommendedPlanTypes: List[str]
    reasons: List[str]
    personalizedPlans: List[PlanRecommendation] = Field(default_factory=list)