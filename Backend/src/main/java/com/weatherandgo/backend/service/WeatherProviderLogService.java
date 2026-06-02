package com.weatherandgo.backend.service;

import com.weatherandgo.backend.entity.WeatherProviderLog;
import com.weatherandgo.backend.model.NormalizedWeatherData;
import com.weatherandgo.backend.repository.WeatherProviderLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WeatherProviderLogService {

    private final WeatherProviderLogRepository weatherProviderLogRepository;

    public WeatherProviderLogService(WeatherProviderLogRepository weatherProviderLogRepository) {
        this.weatherProviderLogRepository = weatherProviderLogRepository;
    }

    public void saveSuccessfulProviderData(NormalizedWeatherData data) {
        WeatherProviderLog log = new WeatherProviderLog(
                data.getProviderName(),
                data.getLatitude(),
                data.getLongitude(),
                data.getCurrentTemperature(),
                data.getCurrentHumidity(),
                data.getCurrentWindSpeed(),
                data.getCurrentPrecipitationProbability(),
                data.getCurrentWeatherStatus(),
                "OK",
                LocalDateTime.now()
        );

        weatherProviderLogRepository.save(log);
    }

    public void saveFailedProviderData(String providerName, Double latitude, Double longitude) {
        WeatherProviderLog log = new WeatherProviderLog(
                providerName,
                latitude,
                longitude,
                null,
                null,
                null,
                null,
                null,
                "FAILED",
                LocalDateTime.now()
        );

        weatherProviderLogRepository.save(log);
    }

    public List<WeatherProviderLog> getLatestProviderLogs() {
        return weatherProviderLogRepository.findTop20ByOrderByCreatedAtDesc();
    }

    public long countProviderLogs() {
        return weatherProviderLogRepository.count();
    }
}