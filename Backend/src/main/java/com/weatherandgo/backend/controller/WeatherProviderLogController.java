package com.weatherandgo.backend.controller;

import com.weatherandgo.backend.entity.WeatherProviderLog;
import com.weatherandgo.backend.service.WeatherProviderLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weather/provider-logs")
public class WeatherProviderLogController {

    private final WeatherProviderLogService weatherProviderLogService;

    public WeatherProviderLogController(WeatherProviderLogService weatherProviderLogService) {
        this.weatherProviderLogService = weatherProviderLogService;
    }

    @GetMapping("/latest")
    public ResponseEntity<List<WeatherProviderLog>> getLatestProviderLogs() {
        return ResponseEntity.ok(weatherProviderLogService.getLatestProviderLogs());
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countProviderLogs() {
        return ResponseEntity.ok(weatherProviderLogService.countProviderLogs());
    }
}