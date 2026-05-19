package com.weatherandgo.backend.dto;

public class DailyForecastResponse {

    private String date;
    private String dayName;
    private Double maxTemperature;
    private Double minTemperature;
    private Integer precipitationProbability;
    private Double windSpeed;
    private String weatherStatus;
    private String recommendation;

    public DailyForecastResponse() {
    }

    public DailyForecastResponse(String date, String dayName, Double maxTemperature, Double minTemperature, Integer precipitationProbability, Double windSpeed, String weatherStatus, String recommendation) {
        this.date = date;
        this.dayName = dayName;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.precipitationProbability = precipitationProbability;
        this.windSpeed = windSpeed;
        this.weatherStatus = weatherStatus;
        this.recommendation = recommendation;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public Double getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(Double maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public Double getMinTemperature() {
        return minTemperature;
    }

    public void setMinTemperature(Double minTemperature) {
        this.minTemperature = minTemperature;
    }

    public Integer getPrecipitationProbability() {
        return precipitationProbability;
    }

    public void setPrecipitationProbability(Integer precipitationProbability) {
        this.precipitationProbability = precipitationProbability;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getWeatherStatus() {
        return weatherStatus;
    }

    public void setWeatherStatus(String weatherStatus) {
        this.weatherStatus = weatherStatus;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}