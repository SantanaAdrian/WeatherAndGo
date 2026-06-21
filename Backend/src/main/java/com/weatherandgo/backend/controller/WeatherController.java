package com.weatherandgo.backend.controller;

import com.weatherandgo.backend.dto.WeatherForecastResponse;
import com.weatherandgo.backend.service.WeatherAggregationService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherAggregationService weatherAggregationService;

    public WeatherController(WeatherAggregationService weatherAggregationService) {
        this.weatherAggregationService = weatherAggregationService;
    }

    @GetMapping("/forecast")
    public ResponseEntity<WeatherForecastResponse> getForecast(
            @RequestParam("lat")
            @NotNull(message = "La latitud es obligatoria")
            @DecimalMin(value = "-90.0", message = "La latitud mínima es -90")
            @DecimalMax(value = "90.0", message = "La latitud máxima es 90")
            Double latitude,

            @RequestParam("lon")
            @NotNull(message = "La longitud es obligatoria")
            @DecimalMin(value = "-180.0", message = "La longitud mínima es -180")
            @DecimalMax(value = "180.0", message = "La longitud máxima es 180")
            Double longitude
    ) {
        WeatherForecastResponse response = weatherAggregationService.getForecast(latitude, longitude);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Weather API running");
    }
}