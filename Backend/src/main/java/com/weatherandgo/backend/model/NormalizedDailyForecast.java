package com.weatherandgo.backend.model;

public class NormalizedDailyForecast {

    private String date;
    private Double maxTemperature;
    private Double minTemperature;
    private Integer precipitationProbability;
    private Double windSpeed;
    private String weatherStatus;

    public NormalizedDailyForecast() {
    }

    public NormalizedDailyForecast(String date, Double maxTemperature, Double minTemperature, Integer precipitationProbability, Double windSpeed, String weatherStatus) {
        this.date = date;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.precipitationProbability = precipitationProbability;
        this.windSpeed = windSpeed;
        this.weatherStatus = weatherStatus;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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
}