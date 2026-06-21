package com.weatherandgo.backend.client;

import com.weatherandgo.backend.model.NormalizedWeatherData;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Order(3)
public class MetNorwayClient implements WeatherProviderClient {

    private static final String PROVIDER_NAME = "MET_NORWAY";

    private final RestClient restClient;

    public MetNorwayClient() {
        this.restClient = RestClient.builder()
                .defaultHeader("User-Agent", "WeatherAndGo-TFE/1.0 santana.txiki@gmail.com")
                .build();
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public NormalizedWeatherData getForecast(Double latitude, Double longitude) {
        String url = buildUrl(latitude, longitude);

        Map<String, Object> response = restClient
                .get()
                .uri(url)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (response == null) {
            throw new IllegalStateException("MET Norway no ha devuelto datos.");
        }

        return mapToNormalizedWeatherData(response, latitude, longitude);
    }

    private String buildUrl(Double latitude, Double longitude) {
        return "https://api.met.no/weatherapi/locationforecast/2.0/compact"
                + "?lat=" + latitude
                + "&lon=" + longitude;
    }

    @SuppressWarnings("unchecked")
    private NormalizedWeatherData mapToNormalizedWeatherData(
            Map<String, Object> response,
            Double latitude,
            Double longitude
    ) {
        Map<String, Object> properties = (Map<String, Object>) response.get("properties");

        if (properties == null) {
            throw new IllegalStateException("Respuesta inválida de MET Norway: properties no encontrado.");
        }

        List<Map<String, Object>> timeseries = (List<Map<String, Object>>) properties.get("timeseries");

        if (timeseries == null || timeseries.isEmpty()) {
            throw new IllegalStateException("Respuesta inválida de MET Norway: timeseries no encontrado.");
        }

        Map<String, Object> currentItem = timeseries.get(0);
        Map<String, Object> data = (Map<String, Object>) currentItem.get("data");

        if (data == null) {
            throw new IllegalStateException("Respuesta inválida de MET Norway: data no encontrado.");
        }

        Map<String, Object> instant = (Map<String, Object>) data.get("instant");

        if (instant == null) {
            throw new IllegalStateException("Respuesta inválida de MET Norway: instant no encontrado.");
        }

        Map<String, Object> details = (Map<String, Object>) instant.get("details");

        if (details == null) {
            throw new IllegalStateException("Respuesta inválida de MET Norway: details no encontrado.");
        }

        NormalizedWeatherData normalizedWeatherData = new NormalizedWeatherData();

        normalizedWeatherData.setProviderName(PROVIDER_NAME);
        normalizedWeatherData.setLatitude(latitude);
        normalizedWeatherData.setLongitude(longitude);
        normalizedWeatherData.setTimezone("MET Norway");

        normalizedWeatherData.setCurrentTemperature(getDouble(details, "air_temperature"));
        normalizedWeatherData.setCurrentHumidity(getInteger(details, "relative_humidity"));
        normalizedWeatherData.setCurrentWindSpeed(convertMetersPerSecondToKilometersPerHour(getDouble(details, "wind_speed")));
        normalizedWeatherData.setCurrentPrecipitationProbability(resolvePrecipitationProbability(data));
        normalizedWeatherData.setCurrentWeatherStatus(resolveWeatherStatus(data));

        normalizedWeatherData.setHourlyForecast(new ArrayList<>());
        normalizedWeatherData.setDailyForecast(new ArrayList<>());

        return normalizedWeatherData;
    }

    @SuppressWarnings("unchecked")
    private Integer resolvePrecipitationProbability(Map<String, Object> data) {
        Map<String, Object> nextOneHour = (Map<String, Object>) data.get("next_1_hours");

        if (nextOneHour == null) {
            return 0;
        }

        Map<String, Object> details = (Map<String, Object>) nextOneHour.get("details");

        if (details == null) {
            return 0;
        }

        Double precipitationAmount = getDouble(details, "precipitation_amount");

        if (precipitationAmount == null || precipitationAmount <= 0) {
            return 0;
        }

        if (precipitationAmount >= 5) {
            return 90;
        }

        if (precipitationAmount >= 2) {
            return 70;
        }

        if (precipitationAmount >= 0.5) {
            return 50;
        }

        return 30;
    }

    @SuppressWarnings("unchecked")
    private String resolveWeatherStatus(Map<String, Object> data) {
        Map<String, Object> nextOneHour = (Map<String, Object>) data.get("next_1_hours");

        if (nextOneHour == null) {
            return "Desconocido";
        }

        Map<String, Object> summary = (Map<String, Object>) nextOneHour.get("summary");

        if (summary == null) {
            return "Desconocido";
        }

        Object symbolCodeObject = summary.get("symbol_code");

        if (symbolCodeObject == null || symbolCodeObject.toString().isBlank()) {
            return "Desconocido";
        }

        return mapSymbolCodeToStatus(symbolCodeObject.toString());
    }

    private String mapSymbolCodeToStatus(String symbolCode) {
        String code = symbolCode.toLowerCase();

        if (code.contains("thunder")) {
            return "Tormenta";
        }

        if (code.contains("snow") || code.contains("sleet")) {
            return "Nieve";
        }

        if (code.contains("rain")) {
            return "Lluvia";
        }

        if (code.contains("fog")) {
            return "Niebla";
        }

        if (code.contains("cloudy")) {
            return "Nuboso";
        }

        if (code.contains("partlycloudy")) {
            return "Parcialmente nuboso";
        }

        if (code.contains("clearsky") || code.contains("fair")) {
            return "Despejado";
        }

        return "Desconocido";
    }

    private Double convertMetersPerSecondToKilometersPerHour(Double value) {
        if (value == null) {
            return null;
        }

        return Math.round(value * 3.6 * 10.0) / 10.0;
    }

    private Double getDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        return null;
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value instanceof Number number) {
            return number.intValue();
        }

        return null;
    }
}