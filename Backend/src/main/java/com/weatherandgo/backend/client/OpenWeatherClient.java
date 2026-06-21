package com.weatherandgo.backend.client;

import com.weatherandgo.backend.model.NormalizedWeatherData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Order(2)
public class OpenWeatherClient implements WeatherProviderClient {

    private static final String PROVIDER_NAME = "OPEN_WEATHER";

    private final RestClient restClient;
    private final String apiKey;

    public OpenWeatherClient(@Value("${openweather.api.key:}") String apiKey) {
        this.restClient = RestClient.create();
        this.apiKey = apiKey;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public NormalizedWeatherData getForecast(Double latitude, Double longitude) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenWeather API key no configurada.");
        }

        String url = buildUrl(latitude, longitude);

        Map<String, Object> response = restClient
                .get()
                .uri(url)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (response == null) {
            throw new IllegalStateException("OpenWeather no ha devuelto datos.");
        }

        return mapToNormalizedWeatherData(response, latitude, longitude);
    }

    private String buildUrl(Double latitude, Double longitude) {
        return "https://api.openweathermap.org/data/2.5/weather"
                + "?lat=" + latitude
                + "&lon=" + longitude
                + "&appid=" + apiKey
                + "&units=metric"
                + "&lang=es";
    }

    @SuppressWarnings("unchecked")
    private NormalizedWeatherData mapToNormalizedWeatherData(
            Map<String, Object> response,
            Double latitude,
            Double longitude
    ) {
        NormalizedWeatherData data = new NormalizedWeatherData();

        data.setProviderName(PROVIDER_NAME);
        data.setLatitude(latitude);
        data.setLongitude(longitude);
        data.setTimezone("OpenWeather");

        Map<String, Object> main = (Map<String, Object>) response.get("main");
        Map<String, Object> wind = (Map<String, Object>) response.get("wind");
        List<Map<String, Object>> weatherList = (List<Map<String, Object>>) response.get("weather");
        Map<String, Object> rain = (Map<String, Object>) response.get("rain");

        if (main != null) {
            data.setCurrentTemperature(getDouble(main, "temp"));
            data.setCurrentHumidity(getInteger(main, "humidity"));
        }

        if (wind != null) {
            Double windMetersPerSecond = getDouble(wind, "speed");
            data.setCurrentWindSpeed(convertMetersPerSecondToKilometersPerHour(windMetersPerSecond));
        }

        data.setCurrentPrecipitationProbability(resolvePrecipitationFromRain(rain));
        data.setCurrentWeatherStatus(resolveWeatherStatus(weatherList));

        data.setHourlyForecast(new ArrayList<>());
        data.setDailyForecast(new ArrayList<>());

        return data;
    }

    private Integer resolvePrecipitationFromRain(Map<String, Object> rain) {
        if (rain == null || rain.isEmpty()) {
            return 0;
        }

        Double rainOneHour = getDouble(rain, "1h");

        if (rainOneHour == null || rainOneHour <= 0) {
            return 0;
        }

        if (rainOneHour >= 5) {
            return 90;
        }

        if (rainOneHour >= 2) {
            return 70;
        }

        if (rainOneHour >= 0.5) {
            return 50;
        }

        return 30;
    }

    private String resolveWeatherStatus(List<Map<String, Object>> weatherList) {
        if (weatherList == null || weatherList.isEmpty()) {
            return "Desconocido";
        }

        Map<String, Object> firstWeather = weatherList.get(0);

        Object description = firstWeather.get("description");

        if (description == null || description.toString().isBlank()) {
            return "Desconocido";
        }

        return capitalize(description.toString());
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

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}