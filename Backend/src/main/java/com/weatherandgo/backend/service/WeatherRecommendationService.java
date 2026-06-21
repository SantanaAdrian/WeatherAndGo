package com.weatherandgo.backend.service;

import org.springframework.stereotype.Service;

@Service
public class WeatherRecommendationService {

    public String generateCurrentRecommendation(
            Integer precipitationProbability,
            Double temperature,
            Double windSpeed,
            String weatherStatus
    ) {
        String status = normalizeStatus(weatherStatus);

        if (containsStorm(status)) {
            return "Riesgo de tormenta. Se recomienda evitar actividades al aire libre y priorizar planes en espacios cerrados.";
        }

        if (precipitationProbability != null && precipitationProbability >= 70) {
            return "Probabilidad de lluvia muy alta. Se recomiendan actividades de interior.";
        }

        if (containsRain(status) || precipitationProbability != null && precipitationProbability >= 50) {
            return "Posibilidad relevante de lluvia. Se recomienda elegir planes de interior o llevar una alternativa cubierta.";
        }

        if (windSpeed != null && windSpeed >= 40) {
            return "Viento fuerte. Se recomienda evitar actividades expuestas, rutas de montaña o planes en zonas abiertas.";
        }

        if (temperature != null && temperature >= 32) {
            return "Temperatura elevada. Se recomiendan planes en sombra, espacios climatizados o actividades suaves.";
        }

        if (temperature != null && temperature <= 5) {
            return "Temperatura baja. Se recomiendan planes de interior o actividades exteriores de corta duración.";
        }

        if (containsFog(status)) {
            return "Presencia de niebla. Se recomienda evitar desplazamientos largos o actividades que dependan de buena visibilidad.";
        }

        if (containsSnow(status)) {
            return "Posibilidad de nieve. Se recomienda precaución en desplazamientos y priorizar planes seguros.";
        }

        if (precipitationProbability != null && precipitationProbability >= 30) {
            return "Condiciones variables. Se recomienda plan mixto con alternativa de interior.";
        }

        if (temperature != null && temperature >= 18 && temperature <= 28 && windSpeed != null && windSpeed < 30) {
            return "Condiciones favorables para actividades al aire libre.";
        }

        return "Condiciones moderadas. Se recomienda revisar la evolución de la previsión antes de planificar actividades.";
    }

    public String generateDailyRecommendation(
            Integer precipitationProbability,
            Double maxTemperature,
            Double windSpeed,
            String weatherStatus
    ) {
        String status = normalizeStatus(weatherStatus);

        if (containsStorm(status)) {
            return "Evitar planes exteriores por posible tormenta. Mejor elegir actividades de interior.";
        }

        if (precipitationProbability != null && precipitationProbability >= 70) {
            return "Mejor priorizar planes de interior por alta probabilidad de lluvia.";
        }

        if (containsRain(status) || precipitationProbability != null && precipitationProbability >= 50) {
            return "Conviene preparar una alternativa cubierta por riesgo de lluvia.";
        }

        if (windSpeed != null && windSpeed >= 40) {
            return "Evitar rutas expuestas o actividades al aire libre por viento fuerte.";
        }

        if (maxTemperature != null && maxTemperature >= 32) {
            return "Evitar actividad física intensa en las horas centrales del día.";
        }

        if (maxTemperature != null && maxTemperature <= 8) {
            return "Día frío. Mejor optar por planes de interior o salidas breves.";
        }

        if (containsFog(status)) {
            return "Día con visibilidad reducida. Se recomienda precaución en desplazamientos.";
        }

        if (containsSnow(status)) {
            return "Posibilidad de nieve. Se recomienda evitar desplazamientos innecesarios.";
        }

        if (precipitationProbability != null && precipitationProbability >= 30) {
            return "Día adecuado para planes mixtos, combinando exterior con alternativa cubierta.";
        }

        return "Día adecuado para planes al aire libre o actividades urbanas.";
    }

    private String normalizeStatus(String weatherStatus) {
        if (weatherStatus == null) {
            return "";
        }

        return weatherStatus.toLowerCase();
    }

    private boolean containsStorm(String status) {
        return status.contains("tormenta");
    }

    private boolean containsRain(String status) {
        return status.contains("lluvia")
                || status.contains("llovizna")
                || status.contains("chubasco");
    }

    private boolean containsFog(String status) {
        return status.contains("niebla");
    }

    private boolean containsSnow(String status) {
        return status.contains("nieve");
    }
}