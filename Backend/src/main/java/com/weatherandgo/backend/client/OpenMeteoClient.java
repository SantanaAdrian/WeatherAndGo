package com.weatherandgo.backend.client;

import com.weatherandgo.backend.model.NormalizedDailyForecast;
import com.weatherandgo.backend.model.NormalizedHourlyForecast;
import com.weatherandgo.backend.model.NormalizedWeatherData;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class OpenMeteoClient implements WeatherProviderClient {

    private static final String PROVIDER_NAME = "OPEN_METEO";

    private final RestClient restClient;

    public OpenMeteoClient() {
        this.restClient = RestClient.create();
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
            throw new IllegalStateException("Open-Meteo no ha devuelto datos.");
        }

        return mapToNormalizedWeatherData(response, latitude, longitude);
    }

    private String buildUrl(Double latitude, Double longitude) {
        return "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + latitude
                + "&longitude=" + longitude
                + "&current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code"
                + "&hourly=temperature_2m,precipitation_probability,wind_speed_10m,weather_code"
                + "&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max,wind_speed_10m_max,weather_code"
                + "&timezone=auto"
                + "&forecast_days=7";
    }

    @SuppressWarnings("unchecked")
    private NormalizedWeatherData mapToNormalizedWeatherData(Map<String, Object> response, Double latitude, Double longitude) {
        NormalizedWeatherData normalizedWeatherData = new NormalizedWeatherData();

        normalizedWeatherData.setProviderName(PROVIDER_NAME);
        normalizedWeatherData.setLatitude(latitude);
        normalizedWeatherData.setLongitude(longitude);
        normalizedWeatherData.setTimezone(getString(response, "timezone"));

        Map<String, Object> current = (Map<String, Object>) response.get("current");

        if (current != null) {
            normalizedWeatherData.setCurrentTemperature(getDouble(current, "temperature_2m"));
            normalizedWeatherData.setCurrentHumidity(getInteger(current, "relative_humidity_2m"));
            normalizedWeatherData.setCurrentWindSpeed(getDouble(current, "wind_speed_10m"));
            normalizedWeatherData.setCurrentPrecipitationProbability(
                resolveCurrentPrecipitationProbability(response, getString(current, "time"))
            );
            normalizedWeatherData.setCurrentWeatherStatus(mapWeatherCodeToStatus(getInteger(current, "weather_code")));
        }

        Map<String, Object> hourly = (Map<String, Object>) response.get("hourly");

        if (hourly != null) {
            normalizedWeatherData.setHourlyForecast(mapHourlyForecast(hourly));
        }

        Map<String, Object> daily = (Map<String, Object>) response.get("daily");

        if (daily != null) {
            normalizedWeatherData.setDailyForecast(mapDailyForecast(daily));
        }

        return normalizedWeatherData;
    }

    @SuppressWarnings("unchecked")
    private List<NormalizedHourlyForecast> mapHourlyForecast(Map<String, Object> hourly) {
        List<NormalizedHourlyForecast> hourlyForecast = new ArrayList<>();

        List<String> times = (List<String>) hourly.get("time");
        List<Number> temperatures = (List<Number>) hourly.get("temperature_2m");
        List<Number> precipitationProbabilities = (List<Number>) hourly.get("precipitation_probability");
        List<Number> windSpeeds = (List<Number>) hourly.get("wind_speed_10m");
        List<Number> weatherCodes = (List<Number>) hourly.get("weather_code");

        if (times == null || temperatures == null || precipitationProbabilities == null || windSpeeds == null || weatherCodes == null) {
            return hourlyForecast;
        }

        int maxItems = Math.min(times.size(), 24);

        for (int i = 0; i < maxItems; i += 3) {
            hourlyForecast.add(new NormalizedHourlyForecast(
                    times.get(i),
                    toDouble(temperatures.get(i)),
                    toInteger(precipitationProbabilities.get(i)),
                    toDouble(windSpeeds.get(i)),
                    mapWeatherCodeToStatus(toInteger(weatherCodes.get(i)))
            ));
        }

        return hourlyForecast;
    }

    @SuppressWarnings("unchecked")
    private List<NormalizedDailyForecast> mapDailyForecast(Map<String, Object> daily) {
        List<NormalizedDailyForecast> dailyForecast = new ArrayList<>();

        List<String> dates = (List<String>) daily.get("time");
        List<Number> maxTemperatures = (List<Number>) daily.get("temperature_2m_max");
        List<Number> minTemperatures = (List<Number>) daily.get("temperature_2m_min");
        List<Number> precipitationProbabilities = (List<Number>) daily.get("precipitation_probability_max");
        List<Number> windSpeeds = (List<Number>) daily.get("wind_speed_10m_max");
        List<Number> weatherCodes = (List<Number>) daily.get("weather_code");

        if (dates == null || maxTemperatures == null || minTemperatures == null || precipitationProbabilities == null || windSpeeds == null || weatherCodes == null) {
            return dailyForecast;
        }

        int maxItems = Math.min(dates.size(), 7);

        for (int i = 0; i < maxItems; i++) {
            dailyForecast.add(new NormalizedDailyForecast(
                    dates.get(i),
                    toDouble(maxTemperatures.get(i)),
                    toDouble(minTemperatures.get(i)),
                    toInteger(precipitationProbabilities.get(i)),
                    toDouble(windSpeeds.get(i)),
                    mapWeatherCodeToStatus(toInteger(weatherCodes.get(i)))
            ));
        }

        return dailyForecast;
    }

    @SuppressWarnings("unchecked")
    private Integer resolveCurrentPrecipitationProbability(Map<String, Object> response, String currentTime) {
        Map<String, Object> hourly = (Map<String, Object>) response.get("hourly");

        if (hourly == null) {
            return 0;
        }

        List<String> times = (List<String>) hourly.get("time");
        List<Number> precipitationProbabilities = (List<Number>) hourly.get("precipitation_probability");

        if (times == null || precipitationProbabilities == null || times.isEmpty() || precipitationProbabilities.isEmpty()) {
            return 0;
        }

        int maxItems = Math.min(times.size(), precipitationProbabilities.size());

        if (currentTime != null && !currentTime.isBlank()) {
            for (int i = 0; i < maxItems; i++) {
                if (currentTime.equals(times.get(i))) {
                    return toInteger(precipitationProbabilities.get(i));
                }
            }

            for (int i = 0; i < maxItems; i++) {
                if (times.get(i).compareTo(currentTime) >= 0) {
                    return toInteger(precipitationProbabilities.get(i));
                }
            }
        }

        return toInteger(precipitationProbabilities.get(0));
    }

    private String mapWeatherCodeToStatus(Integer code) {
        if (code == null) {
            return "Desconocido";
        }

        return switch (code) {
            case 0 -> "Despejado";
            case 1, 2 -> "Parcialmente nuboso";
            case 3 -> "Nuboso";
            case 45, 48 -> "Niebla";
            case 51, 53, 55 -> "Llovizna";
            case 56, 57 -> "Llovizna helada";
            case 61, 63, 65 -> "Lluvia";
            case 66, 67 -> "Lluvia helada";
            case 71, 73, 75 -> "Nieve";
            case 77 -> "Granizo";
            case 80, 81, 82 -> "Chubascos";
            case 85, 86 -> "Chubascos de nieve";
            case 95 -> "Tormenta";
            case 96, 99 -> "Tormenta con granizo";
            default -> "Desconocido";
        };
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
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

    private Double toDouble(Number number) {
        return number != null ? number.doubleValue() : null;
    }

    private Integer toInteger(Number number) {
        return number != null ? number.intValue() : null;
    }
}