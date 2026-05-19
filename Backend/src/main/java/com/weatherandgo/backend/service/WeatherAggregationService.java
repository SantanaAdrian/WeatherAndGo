package com.weatherandgo.backend.service;

import com.weatherandgo.backend.client.WeatherProviderClient;
import com.weatherandgo.backend.dto.DailyForecastResponse;
import com.weatherandgo.backend.dto.HourlyForecastResponse;
import com.weatherandgo.backend.dto.WeatherForecastResponse;
import com.weatherandgo.backend.dto.WeatherSourceResponse;
import com.weatherandgo.backend.model.NormalizedDailyForecast;
import com.weatherandgo.backend.model.NormalizedHourlyForecast;
import com.weatherandgo.backend.model.NormalizedWeatherData;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class WeatherAggregationService {

    private final List<WeatherProviderClient> weatherProviderClients;
    private final WeatherRecommendationService weatherRecommendationService;
    private final LocationService locationService;

    public WeatherAggregationService(
            List<WeatherProviderClient> weatherProviderClients,
            WeatherRecommendationService weatherRecommendationService,
            LocationService locationService
    ) {
        this.weatherProviderClients = weatherProviderClients;
        this.weatherRecommendationService = weatherRecommendationService;
        this.locationService = locationService;
    }

    public WeatherForecastResponse getForecast(Double latitude, Double longitude) {
        List<NormalizedWeatherData> providerDataList = new ArrayList<>();
        List<WeatherSourceResponse> sources = new ArrayList<>();

        for (WeatherProviderClient providerClient : weatherProviderClients) {
            try {
                NormalizedWeatherData providerData = providerClient.getForecast(latitude, longitude);
                providerDataList.add(providerData);
                sources.add(new WeatherSourceResponse(providerClient.getProviderName(), "OK"));
            } catch (Exception exception) {
                sources.add(new WeatherSourceResponse(providerClient.getProviderName(), "FAILED"));
            }
        }

        if (providerDataList.isEmpty()) {
            throw new IllegalStateException("No se han podido obtener datos meteorológicos de ningún proveedor.");
        }

        NormalizedWeatherData aggregatedData = aggregateProviderData(providerDataList);

        WeatherForecastResponse response = mapToResponse(aggregatedData);
        response.setSources(sources);

        return response;
    }

    private NormalizedWeatherData aggregateProviderData(List<NormalizedWeatherData> providerDataList) {
        NormalizedWeatherData firstProviderData = providerDataList.get(0);

        NormalizedWeatherData aggregatedData = new NormalizedWeatherData();

        aggregatedData.setProviderName("AGGREGATED");
        aggregatedData.setLatitude(firstProviderData.getLatitude());
        aggregatedData.setLongitude(firstProviderData.getLongitude());
        aggregatedData.setTimezone(firstProviderData.getTimezone());

        aggregatedData.setCurrentTemperature(calculateAverageTemperature(providerDataList));
        aggregatedData.setCurrentHumidity(calculateAverageHumidity(providerDataList));
        aggregatedData.setCurrentWindSpeed(calculateAverageWindSpeed(providerDataList));
        aggregatedData.setCurrentPrecipitationProbability(calculateMaxPrecipitationProbability(providerDataList));
        aggregatedData.setCurrentWeatherStatus(selectMostRestrictiveWeatherStatus(providerDataList));

        aggregatedData.setHourlyForecast(firstProviderData.getHourlyForecast());
        aggregatedData.setDailyForecast(firstProviderData.getDailyForecast());

        return aggregatedData;
    }

    private WeatherForecastResponse mapToResponse(NormalizedWeatherData data) {
        WeatherForecastResponse response = new WeatherForecastResponse();

        response.setLocationName(locationService.resolveLocationName(data.getLatitude(), data.getLongitude()));
        response.setLatitude(data.getLatitude());
        response.setLongitude(data.getLongitude());
        response.setTimezone(data.getTimezone());

        response.setCurrentTemperature(data.getCurrentTemperature());
        response.setCurrentHumidity(data.getCurrentHumidity());
        response.setCurrentWindSpeed(data.getCurrentWindSpeed());
        response.setCurrentPrecipitationProbability(data.getCurrentPrecipitationProbability());
        response.setCurrentWeatherStatus(data.getCurrentWeatherStatus());

        response.setCurrentRecommendation(
                weatherRecommendationService.generateCurrentRecommendation(
                        data.getCurrentPrecipitationProbability(),
                        data.getCurrentTemperature(),
                        data.getCurrentWindSpeed(),
                        data.getCurrentWeatherStatus()
                )
        );

        response.setHourlyForecast(mapHourlyForecast(data.getHourlyForecast()));
        response.setDailyForecast(mapDailyForecast(data.getDailyForecast()));

        return response;
    }

    private List<HourlyForecastResponse> mapHourlyForecast(List<NormalizedHourlyForecast> hourlyForecast) {
        List<HourlyForecastResponse> response = new ArrayList<>();

        if (hourlyForecast == null) {
            return response;
        }

        for (NormalizedHourlyForecast item : hourlyForecast) {
            response.add(new HourlyForecastResponse(
                    item.getTime(),
                    item.getTemperature(),
                    item.getPrecipitationProbability(),
                    item.getWindSpeed(),
                    item.getWeatherStatus()
            ));
        }

        return response;
    }

    private List<DailyForecastResponse> mapDailyForecast(List<NormalizedDailyForecast> dailyForecast) {
        List<DailyForecastResponse> response = new ArrayList<>();

        if (dailyForecast == null) {
            return response;
        }

        for (NormalizedDailyForecast item : dailyForecast) {
            response.add(new DailyForecastResponse(
                    item.getDate(),
                    getDayName(item.getDate()),
                    item.getMaxTemperature(),
                    item.getMinTemperature(),
                    item.getPrecipitationProbability(),
                    item.getWindSpeed(),
                    item.getWeatherStatus(),
                    weatherRecommendationService.generateDailyRecommendation(
                            item.getPrecipitationProbability(),
                            item.getMaxTemperature(),
                            item.getWindSpeed(),
                            item.getWeatherStatus()
                    )
            ));
        }

        return response;
    }

    private Double calculateAverageTemperature(List<NormalizedWeatherData> providerDataList) {
        return providerDataList.stream()
                .map(NormalizedWeatherData::getCurrentTemperature)
                .filter(value -> value != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);
    }

    private Integer calculateAverageHumidity(List<NormalizedWeatherData> providerDataList) {
        return (int) Math.round(providerDataList.stream()
                .map(NormalizedWeatherData::getCurrentHumidity)
                .filter(value -> value != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0));
    }

    private Double calculateAverageWindSpeed(List<NormalizedWeatherData> providerDataList) {
        return providerDataList.stream()
                .map(NormalizedWeatherData::getCurrentWindSpeed)
                .filter(value -> value != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);
    }

    private Integer calculateMaxPrecipitationProbability(List<NormalizedWeatherData> providerDataList) {
        return providerDataList.stream()
                .map(NormalizedWeatherData::getCurrentPrecipitationProbability)
                .filter(value -> value != null)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
    }

    private String selectMostRestrictiveWeatherStatus(List<NormalizedWeatherData> providerDataList) {
        return providerDataList.stream()
                .map(NormalizedWeatherData::getCurrentWeatherStatus)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse("Desconocido");
    }

    private String getDayName(String date) {
        LocalDate localDate = LocalDate.parse(date);
        String dayName = localDate.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));

        return dayName.substring(0, 1).toUpperCase() + dayName.substring(1);
    }
}