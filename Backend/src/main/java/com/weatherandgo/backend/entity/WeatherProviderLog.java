package com.weatherandgo.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "weather_provider_log")
public class WeatherProviderLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String providerName;

    private Double latitude;

    private Double longitude;

    private Double currentTemperature;

    private Integer currentHumidity;

    private Double currentWindSpeed;

    private Integer currentPrecipitationProbability;

    private String currentWeatherStatus;

    private String status;

    private LocalDateTime createdAt;

    public WeatherProviderLog() {
    }

    public WeatherProviderLog(
            String providerName,
            Double latitude,
            Double longitude,
            Double currentTemperature,
            Integer currentHumidity,
            Double currentWindSpeed,
            Integer currentPrecipitationProbability,
            String currentWeatherStatus,
            String status,
            LocalDateTime createdAt
    ) {
        this.providerName = providerName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.currentTemperature = currentTemperature;
        this.currentHumidity = currentHumidity;
        this.currentWindSpeed = currentWindSpeed;
        this.currentPrecipitationProbability = currentPrecipitationProbability;
        this.currentWeatherStatus = currentWeatherStatus;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getProviderName() {
        return providerName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getCurrentTemperature() {
        return currentTemperature;
    }

    public Integer getCurrentHumidity() {
        return currentHumidity;
    }

    public Double getCurrentWindSpeed() {
        return currentWindSpeed;
    }

    public Integer getCurrentPrecipitationProbability() {
        return currentPrecipitationProbability;
    }

    public String getCurrentWeatherStatus() {
        return currentWeatherStatus;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}