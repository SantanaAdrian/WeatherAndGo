package com.weatherandgo.backend.dto;

public class WeatherProviderDataResponse {

    private String provider;
    private Double temperature;
    private Integer humidity;
    private Double windSpeed;
    private Integer precipitationProbability;
    private String weatherStatus;

    public WeatherProviderDataResponse() {
    }

    public WeatherProviderDataResponse(
            String provider,
            Double temperature,
            Integer humidity,
            Double windSpeed,
            Integer precipitationProbability,
            String weatherStatus
    ) {
        this.provider = provider;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.precipitationProbability = precipitationProbability;
        this.weatherStatus = weatherStatus;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public Integer getPrecipitationProbability() {
        return precipitationProbability;
    }

    public void setPrecipitationProbability(Integer precipitationProbability) {
        this.precipitationProbability = precipitationProbability;
    }

    public String getWeatherStatus() {
        return weatherStatus;
    }

    public void setWeatherStatus(String weatherStatus) {
        this.weatherStatus = weatherStatus;
    }
}