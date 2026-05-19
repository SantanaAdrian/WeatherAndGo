package com.weatherandgo.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class WeatherForecastResponse {

    private String locationName;
    private Double latitude;
    private Double longitude;
    private String timezone;

    private Double currentTemperature;
    private Integer currentHumidity;
    private Double currentWindSpeed;
    private Integer currentPrecipitationProbability;
    private String currentWeatherStatus;
    private String currentRecommendation;

    private List<HourlyForecastResponse> hourlyForecast;
    private List<DailyForecastResponse> dailyForecast;
    private List<WeatherSourceResponse> sources;

    public WeatherForecastResponse() {
        this.hourlyForecast = new ArrayList<>();
        this.dailyForecast = new ArrayList<>();
        this.sources = new ArrayList<>();
    }

    public WeatherForecastResponse(
            String locationName,
            Double latitude,
            Double longitude,
            String timezone,
            Double currentTemperature,
            Integer currentHumidity,
            Double currentWindSpeed,
            Integer currentPrecipitationProbability,
            String currentWeatherStatus,
            String currentRecommendation,
            List<HourlyForecastResponse> hourlyForecast,
            List<DailyForecastResponse> dailyForecast,
            List<WeatherSourceResponse> sources
    ) {
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timezone = timezone;
        this.currentTemperature = currentTemperature;
        this.currentHumidity = currentHumidity;
        this.currentWindSpeed = currentWindSpeed;
        this.currentPrecipitationProbability = currentPrecipitationProbability;
        this.currentWeatherStatus = currentWeatherStatus;
        this.currentRecommendation = currentRecommendation;
        this.hourlyForecast = hourlyForecast;
        this.dailyForecast = dailyForecast;
        this.sources = sources;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Double getCurrentTemperature() {
        return currentTemperature;
    }

    public void setCurrentTemperature(Double currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    public Integer getCurrentHumidity() {
        return currentHumidity;
    }

    public void setCurrentHumidity(Integer currentHumidity) {
        this.currentHumidity = currentHumidity;
    }

    public Double getCurrentWindSpeed() {
        return currentWindSpeed;
    }

    public void setCurrentWindSpeed(Double currentWindSpeed) {
        this.currentWindSpeed = currentWindSpeed;
    }

    public Integer getCurrentPrecipitationProbability() {
        return currentPrecipitationProbability;
    }

    public void setCurrentPrecipitationProbability(Integer currentPrecipitationProbability) {
        this.currentPrecipitationProbability = currentPrecipitationProbability;
    }

    public String getCurrentWeatherStatus() {
        return currentWeatherStatus;
    }

    public void setCurrentWeatherStatus(String currentWeatherStatus) {
        this.currentWeatherStatus = currentWeatherStatus;
    }

    public String getCurrentRecommendation() {
        return currentRecommendation;
    }

    public void setCurrentRecommendation(String currentRecommendation) {
        this.currentRecommendation = currentRecommendation;
    }

    public List<HourlyForecastResponse> getHourlyForecast() {
        return hourlyForecast;
    }

    public void setHourlyForecast(List<HourlyForecastResponse> hourlyForecast) {
        this.hourlyForecast = hourlyForecast;
    }

    public List<DailyForecastResponse> getDailyForecast() {
        return dailyForecast;
    }

    public void setDailyForecast(List<DailyForecastResponse> dailyForecast) {
        this.dailyForecast = dailyForecast;
    }

    public List<WeatherSourceResponse> getSources() {
        return sources;
    }

    public void setSources(List<WeatherSourceResponse> sources) {
        this.sources = sources;
    }
}