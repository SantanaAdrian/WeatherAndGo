package com.weatherandgo.backend.dto;

public class WeatherSourceResponse {

    private String provider;
    private String status;

    public WeatherSourceResponse() {
    }

    public WeatherSourceResponse(String provider, String status) {
        this.provider = provider;
        this.status = status;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}