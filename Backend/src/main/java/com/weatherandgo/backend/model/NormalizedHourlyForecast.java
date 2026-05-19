package com.weatherandgo.backend.model;

public class NormalizedHourlyForecast {

    private String time;
    private Double temperature;
    private Integer precipitationProbability;
    private Double windSpeed;
    private String weatherStatus;

    public NormalizedHourlyForecast() {
    }

    public NormalizedHourlyForecast(String time, Double temperature, Integer precipitationProbability, Double windSpeed, String weatherStatus) {
        this.time = time;
        this.temperature = temperature;
        this.precipitationProbability = precipitationProbability;
        this.windSpeed = windSpeed;
        this.weatherStatus = weatherStatus;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
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