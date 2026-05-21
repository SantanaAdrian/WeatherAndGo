package com.weatherandgo.backend.controller;

import com.weatherandgo.backend.entity.WeatherQueryLog;
import com.weatherandgo.backend.repository.WeatherQueryLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weather/logs")
public class WeatherQueryLogController {

    private final WeatherQueryLogRepository weatherQueryLogRepository;

    public WeatherQueryLogController(WeatherQueryLogRepository weatherQueryLogRepository) {
        this.weatherQueryLogRepository = weatherQueryLogRepository;
    }

    @GetMapping
    public ResponseEntity<List<WeatherQueryLog>> getAllLogs() {
        return ResponseEntity.ok(weatherQueryLogRepository.findAll());
    }

    @GetMapping("/latest")
    public ResponseEntity<List<WeatherQueryLog>> getLatestLogs() {
        return ResponseEntity.ok(weatherQueryLogRepository.findTop10ByOrderByCreatedAtDesc());
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countLogs() {
        return ResponseEntity.ok(weatherQueryLogRepository.count());
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllLogs() {
        weatherQueryLogRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}