package com.weatherandgo.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "weather_query_log")
public class WeatherQueryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double latitude;

    private Double longitude;

    private String locationName;

    private Double currentTemperature;

    private Integer currentHumidity;

    private Double currentWindSpeed;

    private Integer currentPrecipitationProbability;

    private String currentWeatherStatus;

    private String providers;

    private LocalDateTime createdAt;

    public WeatherQueryLog() {
    }

    public WeatherQueryLog(
            Double latitude,
            Double longitude,
            String locationName,
            Double currentTemperature,
            Integer currentHumidity,
            Double currentWindSpeed,
            Integer currentPrecipitationProbability,
            String currentWeatherStatus,
            String providers,
            LocalDateTime createdAt
    ) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
        this.currentTemperature = currentTemperature;
        this.currentHumidity = currentHumidity;
        this.currentWindSpeed = currentWindSpeed;
        this.currentPrecipitationProbability = currentPrecipitationProbability;
        this.currentWeatherStatus = currentWeatherStatus;
        this.providers = providers;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getLocationName() {
        return locationName;
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

    public String getProviders() {
        return providers;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}