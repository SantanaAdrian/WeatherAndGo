package com.weatherandgo.backend.service;

import com.weatherandgo.backend.dto.PlanRecommendationResponse;
import com.weatherandgo.backend.dto.WeatherAggregationSummaryResponse;
import com.weatherandgo.backend.model.NormalizedWeatherData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecommendationEngineClient {

    private final RestClient restClient;

    public RecommendationEngineClient(
            @Value("${recommendation.engine.base-url:http://localhost:8001}") String recommendationEngineBaseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(recommendationEngineBaseUrl)
                .build();
    }

    public PlanRecommendationResponse generatePlanRecommendation(
            String locationName,
            NormalizedWeatherData aggregatedData,
            List<NormalizedWeatherData> providerDataList,
            WeatherAggregationSummaryResponse aggregationSummary,
            String fallbackRecommendation
    ) {
        try {
            Map<String, Object> requestBody = buildRequestBody(
                    locationName,
                    aggregatedData,
                    providerDataList,
                    aggregationSummary
            );

            PlanRecommendationResponse response = restClient
                    .post()
                    .uri("/recommendations/weather-plan")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(PlanRecommendationResponse.class);

            if (response == null || response.getSummary() == null || response.getSummary().isBlank()) {
                return buildFallbackRecommendation(fallbackRecommendation);
            }

            return response;

        } catch (Exception exception) {
            System.err.println("No se ha podido obtener recomendación del motor Python: " + exception.getMessage());
            return buildFallbackRecommendation(fallbackRecommendation);
        }
    }

    private Map<String, Object> buildRequestBody(
            String locationName,
            NormalizedWeatherData aggregatedData,
            List<NormalizedWeatherData> providerDataList,
            WeatherAggregationSummaryResponse aggregationSummary
    ) {
        Map<String, Object> requestBody = new HashMap<>();

        requestBody.put("locationName", locationName);
        requestBody.put("currentTemperature", aggregatedData.getCurrentTemperature());
        requestBody.put("currentHumidity", aggregatedData.getCurrentHumidity());
        requestBody.put("currentWindSpeed", aggregatedData.getCurrentWindSpeed());
        requestBody.put("currentPrecipitationProbability", aggregatedData.getCurrentPrecipitationProbability());
        requestBody.put("currentWeatherStatus", aggregatedData.getCurrentWeatherStatus());
        requestBody.put("aggregationSummary", buildAggregationSummaryBody(aggregationSummary));
        requestBody.put("providerData", buildProviderDataBody(providerDataList));

        return requestBody;
    }

    private Map<String, Object> buildAggregationSummaryBody(WeatherAggregationSummaryResponse aggregationSummary) {
        Map<String, Object> body = new HashMap<>();

        if (aggregationSummary == null) {
            body.put("providerCount", 0);
            body.put("temperatureDifference", 0.0);
            body.put("humidityDifference", 0);
            body.put("windSpeedDifference", 0.0);
            body.put("precipitationDifference", 0);
            body.put("reliabilityLevel", "MEDIA");
            body.put("explanation", "No se ha podido calcular la fiabilidad de la agregación.");
            return body;
        }

        body.put("providerCount", aggregationSummary.getProviderCount());
        body.put("temperatureDifference", aggregationSummary.getTemperatureDifference());
        body.put("humidityDifference", aggregationSummary.getHumidityDifference());
        body.put("windSpeedDifference", aggregationSummary.getWindSpeedDifference());
        body.put("precipitationDifference", aggregationSummary.getPrecipitationDifference());
        body.put("reliabilityLevel", aggregationSummary.getReliabilityLevel());
        body.put("explanation", aggregationSummary.getExplanation());

        return body;
    }

    private List<Map<String, Object>> buildProviderDataBody(List<NormalizedWeatherData> providerDataList) {
        List<Map<String, Object>> providerDataBody = new ArrayList<>();

        if (providerDataList == null) {
            return providerDataBody;
        }

        for (NormalizedWeatherData providerData : providerDataList) {
            Map<String, Object> item = new HashMap<>();

            item.put("provider", providerData.getProviderName());
            item.put("temperature", providerData.getCurrentTemperature());
            item.put("humidity", providerData.getCurrentHumidity());
            item.put("windSpeed", providerData.getCurrentWindSpeed());
            item.put("precipitationProbability", providerData.getCurrentPrecipitationProbability());
            item.put("weatherStatus", providerData.getCurrentWeatherStatus());

            providerDataBody.add(item);
        }

        return providerDataBody;
    }

    private PlanRecommendationResponse buildFallbackRecommendation(String fallbackRecommendation) {
        List<String> planTypes = new ArrayList<>();
        planTypes.add("plan flexible");
        planTypes.add("actividad urbana");
        planTypes.add("alternativa cubierta");

        List<String> reasons = new ArrayList<>();
        reasons.add("Se ha usado la recomendación interna por reglas porque el motor Python no está disponible.");

        return new PlanRecommendationResponse(
                "MIXTO",
                50,
                fallbackRecommendation,
                planTypes,
                reasons
        );
    }
}