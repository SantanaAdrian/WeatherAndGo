package com.weatherandgo.backend.model;

import java.util.ArrayList;
import java.util.List;

public class NormalizedWeatherData {

    private String providerName;
    private Double latitude;
    private Double longitude;
    private String timezone;

    private Double currentTemperature;
    private Integer currentHumidity;
    private Double currentWindSpeed;
    private Integer currentPrecipitationProbability;
    private String currentWeatherStatus;

    private List<NormalizedHourlyForecast> hourlyForecast;
    private List<NormalizedDailyForecast> dailyForecast;

    public NormalizedWeatherData() {
        this.hourlyForecast = new ArrayList<>();
        this.dailyForecast = new ArrayList<>();
    }

    public NormalizedWeatherData(
            String providerName,
            Double latitude,
            Double longitude,
            String timezone,
            Double currentTemperature,
            Integer currentHumidity,
            Double currentWindSpeed,
            Integer currentPrecipitationProbability,
            String currentWeatherStatus,
            List<NormalizedHourlyForecast> hourlyForecast,
            List<NormalizedDailyForecast> dailyForecast
    ) {
        this.providerName = providerName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timezone = timezone;
        this.currentTemperature = currentTemperature;
        this.currentHumidity = currentHumidity;
        this.currentWindSpeed = currentWindSpeed;
        this.currentPrecipitationProbability = currentPrecipitationProbability;
        this.currentWeatherStatus = currentWeatherStatus;
        this.hourlyForecast = hourlyForecast;
        this.dailyForecast = dailyForecast;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
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

    public List<NormalizedHourlyForecast> getHourlyForecast() {
        return hourlyForecast;
    }

    public void setHourlyForecast(List<NormalizedHourlyForecast> hourlyForecast) {
        this.hourlyForecast = hourlyForecast;
    }

    public List<NormalizedDailyForecast> getDailyForecast() {
        return dailyForecast;
    }

    public void setDailyForecast(List<NormalizedDailyForecast> dailyForecast) {
        this.dailyForecast = dailyForecast;
    }
}