package com.weatherandgo.backend.service;

import org.springframework.stereotype.Service;

@Service
public class WeatherRecommendationService {

    public String generateCurrentRecommendation(Integer precipitationProbability, Double temperature, Double windSpeed, String weatherStatus) {
        if (precipitationProbability != null && precipitationProbability >= 60) {
            return "Alta probabilidad de lluvia. Se recomiendan actividades de interior.";
        }

        if (weatherStatus != null && weatherStatus.toLowerCase().contains("tormenta")) {
            return "Riesgo de tormenta. Se recomienda evitar actividades al aire libre.";
        }

        if (temperature != null && temperature >= 30) {
            return "Temperatura elevada. Se recomiendan planes en sombra o espacios climatizados.";
        }

        if (windSpeed != null && windSpeed >= 35) {
            return "Viento fuerte. Se recomienda evitar actividades expuestas al aire libre.";
        }

        if (temperature != null && temperature >= 18 && temperature <= 27) {
            return "Condiciones favorables para actividades al aire libre.";
        }

        return "Condiciones moderadas. Se recomienda revisar la previsión antes de planificar actividades.";
    }

    public String generateDailyRecommendation(Integer precipitationProbability, Double maxTemperature, Double windSpeed, String weatherStatus) {
        if (precipitationProbability != null && precipitationProbability >= 60) {
            return "Mejor priorizar planes de interior por alta probabilidad de lluvia.";
        }

        if (weatherStatus != null && weatherStatus.toLowerCase().contains("tormenta")) {
            return "Evitar planes exteriores por posible tormenta.";
        }

        if (maxTemperature != null && maxTemperature >= 30) {
            return "Evitar actividad física intensa en las horas centrales del día.";
        }

        if (windSpeed != null && windSpeed >= 35) {
            return "Conviene evitar rutas expuestas por viento fuerte.";
        }

        return "Día adecuado para planes al aire libre o actividades urbanas.";
    }
}