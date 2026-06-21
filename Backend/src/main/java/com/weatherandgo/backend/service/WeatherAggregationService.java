package com.weatherandgo.backend.service;

import com.weatherandgo.backend.client.WeatherProviderClient;
import com.weatherandgo.backend.dto.DailyForecastResponse;
import com.weatherandgo.backend.dto.HourlyForecastResponse;
import com.weatherandgo.backend.dto.WeatherAggregationSummaryResponse;
import com.weatherandgo.backend.dto.WeatherForecastResponse;
import com.weatherandgo.backend.dto.WeatherProviderDataResponse;
import com.weatherandgo.backend.dto.WeatherSourceResponse;
import com.weatherandgo.backend.dto.PlanRecommendationResponse;
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
    private final WeatherQueryLogService weatherQueryLogService;
    private final WeatherProviderLogService weatherProviderLogService;
    private final RecommendationEngineClient recommendationEngineClient;

    public WeatherAggregationService(
        List<WeatherProviderClient> weatherProviderClients,
        WeatherRecommendationService weatherRecommendationService,
        RecommendationEngineClient recommendationEngineClient,
        LocationService locationService,
        WeatherQueryLogService weatherQueryLogService,
        WeatherProviderLogService weatherProviderLogService
    ) {
        this.weatherProviderClients = weatherProviderClients;
        this.weatherRecommendationService = weatherRecommendationService;
        this.recommendationEngineClient = recommendationEngineClient;
        this.locationService = locationService;
        this.weatherQueryLogService = weatherQueryLogService;
        this.weatherProviderLogService = weatherProviderLogService;
    }

    public WeatherForecastResponse getForecast(Double latitude, Double longitude) {
        List<NormalizedWeatherData> providerDataList = new ArrayList<>();
        List<WeatherSourceResponse> sources = new ArrayList<>();

        for (WeatherProviderClient providerClient : weatherProviderClients) {
            try {
                NormalizedWeatherData providerData = providerClient.getForecast(latitude, longitude);

                providerDataList.add(providerData);
                sources.add(new WeatherSourceResponse(providerClient.getProviderName(), "OK"));

                weatherProviderLogService.saveSuccessfulProviderData(providerData);

            } catch (Exception exception) {
                sources.add(new WeatherSourceResponse(providerClient.getProviderName(), "FAILED"));

                weatherProviderLogService.saveFailedProviderData(
                        providerClient.getProviderName(),
                        latitude,
                        longitude
                );
            }
        }

        if (providerDataList.isEmpty()) {
            throw new IllegalStateException("No se han podido obtener datos meteorológicos de ningún proveedor.");
        }

        NormalizedWeatherData aggregatedData = aggregateProviderData(providerDataList);

        WeatherForecastResponse response = mapToResponse(aggregatedData, providerDataList);
        response.setSources(sources);

        weatherQueryLogService.saveQuery(response);

        return response;
    }

    private NormalizedWeatherData aggregateProviderData(List<NormalizedWeatherData> providerDataList) {
        NormalizedWeatherData baseProviderData = findProviderWithForecastData(providerDataList);

        NormalizedWeatherData aggregatedData = new NormalizedWeatherData();

        aggregatedData.setProviderName("AGGREGATED");
        aggregatedData.setLatitude(baseProviderData.getLatitude());
        aggregatedData.setLongitude(baseProviderData.getLongitude());
        aggregatedData.setTimezone(baseProviderData.getTimezone());

        aggregatedData.setCurrentTemperature(calculateAverageTemperature(providerDataList));
        aggregatedData.setCurrentHumidity(calculateAverageHumidity(providerDataList));
        aggregatedData.setCurrentWindSpeed(calculateAverageWindSpeed(providerDataList));
        aggregatedData.setCurrentPrecipitationProbability(calculateConservativePrecipitationProbability(providerDataList));
        aggregatedData.setCurrentWeatherStatus(selectMostRestrictiveWeatherStatus(providerDataList));

        aggregatedData.setHourlyForecast(baseProviderData.getHourlyForecast());
        aggregatedData.setDailyForecast(baseProviderData.getDailyForecast());

        return aggregatedData;
    }

    private NormalizedWeatherData findProviderWithForecastData(List<NormalizedWeatherData> providerDataList) {
        for (NormalizedWeatherData providerData : providerDataList) {
            boolean hasHourlyForecast = providerData.getHourlyForecast() != null
                    && !providerData.getHourlyForecast().isEmpty();

            boolean hasDailyForecast = providerData.getDailyForecast() != null
                    && !providerData.getDailyForecast().isEmpty();

            if (hasHourlyForecast && hasDailyForecast) {
                return providerData;
            }
        }

        return providerDataList.get(0);
    }

    private WeatherForecastResponse mapToResponse(
            NormalizedWeatherData data,
            List<NormalizedWeatherData> providerDataList
    ) {
        WeatherForecastResponse response = new WeatherForecastResponse();

        String locationName = locationService.resolveLocationName(data.getLatitude(), data.getLongitude());

        response.setLocationName(locationName);
        response.setLatitude(data.getLatitude());
        response.setLongitude(data.getLongitude());
        response.setTimezone(data.getTimezone());

        response.setCurrentTemperature(data.getCurrentTemperature());
        response.setCurrentHumidity(data.getCurrentHumidity());
        response.setCurrentWindSpeed(data.getCurrentWindSpeed());
        response.setCurrentPrecipitationProbability(data.getCurrentPrecipitationProbability());
        response.setCurrentWeatherStatus(data.getCurrentWeatherStatus());

        WeatherAggregationSummaryResponse aggregationSummary = buildAggregationSummary(providerDataList);

        String fallbackRecommendation = weatherRecommendationService.generateCurrentRecommendation(
                data.getCurrentPrecipitationProbability(),
                data.getCurrentTemperature(),
                data.getCurrentWindSpeed(),
                data.getCurrentWeatherStatus()
        );

        PlanRecommendationResponse planRecommendation = recommendationEngineClient.generatePlanRecommendation(
                locationName,
                data,
                providerDataList,
                aggregationSummary,
                fallbackRecommendation
        );

        response.setCurrentRecommendation(planRecommendation.getSummary());

        response.setHourlyForecast(mapHourlyForecast(data.getHourlyForecast()));
        response.setDailyForecast(mapDailyForecast(data.getDailyForecast()));
        response.setProviderData(mapProviderData(providerDataList));
        response.setAggregationSummary(aggregationSummary);
        response.setPlanRecommendation(planRecommendation);

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

    private List<WeatherProviderDataResponse> mapProviderData(List<NormalizedWeatherData> providerDataList) {
        List<WeatherProviderDataResponse> response = new ArrayList<>();

        if (providerDataList == null) {
            return response;
        }

        for (NormalizedWeatherData providerData : providerDataList) {
            response.add(new WeatherProviderDataResponse(
                    providerData.getProviderName(),
                    providerData.getCurrentTemperature(),
                    providerData.getCurrentHumidity(),
                    providerData.getCurrentWindSpeed(),
                    providerData.getCurrentPrecipitationProbability(),
                    providerData.getCurrentWeatherStatus()
            ));
        }

        return response;
    }

    private WeatherAggregationSummaryResponse buildAggregationSummary(List<NormalizedWeatherData> providerDataList) {
        int providerCount = providerDataList == null ? 0 : providerDataList.size();

        Double temperatureDifference = calculateTemperatureDifference(providerDataList);
        Integer humidityDifference = calculateHumidityDifference(providerDataList);
        Double windSpeedDifference = calculateWindSpeedDifference(providerDataList);
        Integer precipitationDifference = calculatePrecipitationDifference(providerDataList);

        String reliabilityLevel = calculateReliabilityLevel(
                providerCount,
                temperatureDifference,
                humidityDifference,
                windSpeedDifference,
                precipitationDifference
        );

        String explanation = buildReliabilityExplanation(reliabilityLevel, providerCount);

        return new WeatherAggregationSummaryResponse(
                providerCount,
                temperatureDifference,
                humidityDifference,
                windSpeedDifference,
                precipitationDifference,
                reliabilityLevel,
                explanation
        );
    }

    private Double calculateAverageTemperature(List<NormalizedWeatherData> providerDataList) {
        double average = providerDataList.stream()
                .map(NormalizedWeatherData::getCurrentTemperature)
                .filter(value -> value != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        return roundOneDecimal(average);
    }

    private Integer calculateAverageHumidity(List<NormalizedWeatherData> providerDataList) {
        double average = providerDataList.stream()
                .map(NormalizedWeatherData::getCurrentHumidity)
                .filter(value -> value != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        return (int) Math.round(average);
    }

    private Double calculateAverageWindSpeed(List<NormalizedWeatherData> providerDataList) {
        double average = providerDataList.stream()
                .map(NormalizedWeatherData::getCurrentWindSpeed)
                .filter(value -> value != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        return roundOneDecimal(average);
    }

    private Integer calculateConservativePrecipitationProbability(List<NormalizedWeatherData> providerDataList) {
        return providerDataList.stream()
                .map(NormalizedWeatherData::getCurrentPrecipitationProbability)
                .filter(value -> value != null)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
    }

    private String selectMostRestrictiveWeatherStatus(List<NormalizedWeatherData> providerDataList) {
        String selectedStatus = "Desconocido";
        int highestSeverity = -1;

        for (NormalizedWeatherData providerData : providerDataList) {
            String status = providerData.getCurrentWeatherStatus();

            if (status == null || status.isBlank()) {
                continue;
            }

            int severity = getWeatherSeverity(status);

            if (severity > highestSeverity) {
                highestSeverity = severity;
                selectedStatus = status;
            }
        }

        return selectedStatus;
    }

    private int getWeatherSeverity(String weatherStatus) {
        String status = weatherStatus.toLowerCase();

        if (status.contains("tormenta")) {
            return 100;
        }

        if (status.contains("nieve")) {
            return 90;
        }

        if (status.contains("granizo")) {
            return 85;
        }

        if (status.contains("lluvia") || status.contains("llovizna") || status.contains("chubasco")) {
            return 80;
        }

        if (status.contains("niebla")) {
            return 70;
        }

        if (status.contains("nuboso") || status.contains("nubes")) {
            return 50;
        }

        if (status.contains("parcialmente")) {
            return 40;
        }

        if (status.contains("despejado") || status.contains("soleado") || status.contains("cielo claro")) {
            return 10;
        }

        return 0;
    }

    private Double calculateTemperatureDifference(List<NormalizedWeatherData> providerDataList) {
        List<Double> values = providerDataList.stream()
                .map(NormalizedWeatherData::getCurrentTemperature)
                .filter(value -> value != null)
                .toList();

        if (values.size() < 2) {
            return 0.0;
        }

        double min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(0);

        return roundOneDecimal(max - min);
    }

    private Integer calculateHumidityDifference(List<NormalizedWeatherData> providerDataList) {
        List<Integer> values = providerDataList.stream()
                .map(NormalizedWeatherData::getCurrentHumidity)
                .filter(value -> value != null)
                .toList();

        if (values.size() < 2) {
            return 0;
        }

        int min = values.stream().mapToInt(Integer::intValue).min().orElse(0);
        int max = values.stream().mapToInt(Integer::intValue).max().orElse(0);

        return max - min;
    }

    private Double calculateWindSpeedDifference(List<NormalizedWeatherData> providerDataList) {
        List<Double> values = providerDataList.stream()
                .map(NormalizedWeatherData::getCurrentWindSpeed)
                .filter(value -> value != null)
                .toList();

        if (values.size() < 2) {
            return 0.0;
        }

        double min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(0);

        return roundOneDecimal(max - min);
    }

    private Integer calculatePrecipitationDifference(List<NormalizedWeatherData> providerDataList) {
        List<Integer> values = providerDataList.stream()
                .map(NormalizedWeatherData::getCurrentPrecipitationProbability)
                .filter(value -> value != null)
                .toList();

        if (values.size() < 2) {
            return 0;
        }

        int min = values.stream().mapToInt(Integer::intValue).min().orElse(0);
        int max = values.stream().mapToInt(Integer::intValue).max().orElse(0);

        return max - min;
    }

    private String calculateReliabilityLevel(
            int providerCount,
            Double temperatureDifference,
            Integer humidityDifference,
            Double windSpeedDifference,
            Integer precipitationDifference
    ) {
        if (providerCount < 2) {
            return "MEDIA";
        }

        boolean highReliability =
                temperatureDifference <= 2.5
                        && humidityDifference <= 15
                        && windSpeedDifference <= 10
                        && precipitationDifference <= 25;

        if (highReliability) {
            return "ALTA";
        }

        boolean mediumReliability =
                temperatureDifference <= 5
                        && humidityDifference <= 25
                        && windSpeedDifference <= 20
                        && precipitationDifference <= 50;

        if (mediumReliability) {
            return "MEDIA";
        }

        return "BAJA";
    }

    private String buildReliabilityExplanation(String reliabilityLevel, int providerCount) {
        if (providerCount < 2) {
            return "La predicción se basa en un único proveedor disponible, por lo que no se puede realizar una comparación completa entre fuentes.";
        }

        if ("ALTA".equals(reliabilityLevel)) {
            return "Los proveedores consultados ofrecen valores similares, por lo que la predicción agregada presenta una fiabilidad alta.";
        }

        if ("MEDIA".equals(reliabilityLevel)) {
            return "Los proveedores consultados presentan algunas diferencias, aunque dentro de un margen aceptable para generar una predicción agregada.";
        }

        return "Los proveedores consultados presentan diferencias relevantes. Se recomienda interpretar la predicción agregada con precaución.";
    }

    private Double roundOneDecimal(Double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private String getDayName(String date) {
        LocalDate localDate = LocalDate.parse(date);
        String dayName = localDate.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));

        return dayName.substring(0, 1).toUpperCase() + dayName.substring(1);
    }
}