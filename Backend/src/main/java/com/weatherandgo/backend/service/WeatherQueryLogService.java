package com.weatherandgo.backend.service;

import com.weatherandgo.backend.dto.WeatherForecastResponse;
import com.weatherandgo.backend.dto.WeatherSourceResponse;
import com.weatherandgo.backend.entity.WeatherQueryLog;
import com.weatherandgo.backend.repository.WeatherQueryLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class WeatherQueryLogService {

    private final WeatherQueryLogRepository weatherQueryLogRepository;

    public WeatherQueryLogService(WeatherQueryLogRepository weatherQueryLogRepository) {
        this.weatherQueryLogRepository = weatherQueryLogRepository;
    }

    public void saveQuery(WeatherForecastResponse response) {
        try {
            String providers = response.getSources()
                    .stream()
                    .map(this::formatProvider)
                    .collect(Collectors.joining(", "));

            WeatherQueryLog log = new WeatherQueryLog(
                    response.getLatitude(),
                    response.getLongitude(),
                    response.getLocationName(),
                    response.getCurrentTemperature(),
                    response.getCurrentHumidity(),
                    response.getCurrentWindSpeed(),
                    response.getCurrentPrecipitationProbability(),
                    response.getCurrentWeatherStatus(),
                    providers,
                    LocalDateTime.now()
            );

            weatherQueryLogRepository.save(log);

        } catch (Exception exception) {
            System.err.println("No se ha podido guardar el registro de consulta meteorológica: " + exception.getMessage());
        }
    }

    private String formatProvider(WeatherSourceResponse source) {
        return source.getProvider() + ":" + source.getStatus();
    }
}