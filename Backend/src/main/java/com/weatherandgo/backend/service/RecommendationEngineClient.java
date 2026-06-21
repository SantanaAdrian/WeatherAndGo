package com.weatherandgo.backend.service;

import com.weatherandgo.backend.dto.PersonalizedPlanResponse;
import com.weatherandgo.backend.dto.PlanRecommendationResponse;
import com.weatherandgo.backend.dto.WeatherAggregationSummaryResponse;
import com.weatherandgo.backend.model.NormalizedWeatherData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
                return buildFallbackRecommendation(locationName, fallbackRecommendation, aggregatedData, aggregationSummary);
            }

            if (!hasConcretePlaces(response.getPersonalizedPlans())) {
                response.setPersonalizedPlans(buildPersonalizedPlans(locationName, aggregatedData, aggregationSummary));
            }

            return response;

        } catch (Exception exception) {
            System.err.println("No se ha podido obtener recomendación del motor Python: " + exception.getMessage());
            return buildFallbackRecommendation(locationName, fallbackRecommendation, aggregatedData, aggregationSummary);
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

    private PlanRecommendationResponse buildFallbackRecommendation(
            String locationName,
            String fallbackRecommendation,
            NormalizedWeatherData aggregatedData,
            WeatherAggregationSummaryResponse aggregationSummary
    ) {
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
                reasons,
                buildPersonalizedPlans(locationName, aggregatedData, aggregationSummary)
        );
    }

    private boolean hasConcretePlaces(List<PersonalizedPlanResponse> personalizedPlans) {
        if (personalizedPlans == null || personalizedPlans.isEmpty()) {
            return false;
        }

        for (PersonalizedPlanResponse plan : personalizedPlans) {
            if (plan.getPlaceName() != null && !plan.getPlaceName().isBlank()) {
                return true;
            }
        }

        return false;
    }

    private List<PersonalizedPlanResponse> buildPersonalizedPlans(
            String locationName,
            NormalizedWeatherData aggregatedData,
            WeatherAggregationSummaryResponse aggregationSummary
    ) {
        LocalPlanCatalog catalog = resolveLocalPlanCatalog(locationName);

        double temperature = aggregatedData.getCurrentTemperature();
        double windSpeed = aggregatedData.getCurrentWindSpeed();
        int precipitationProbability = aggregatedData.getCurrentPrecipitationProbability();
        String weatherStatus = aggregatedData.getCurrentWeatherStatus() == null
                ? ""
                : aggregatedData.getCurrentWeatherStatus().toLowerCase();

        String reliabilityLevel = aggregationSummary == null || aggregationSummary.getReliabilityLevel() == null
                ? "MEDIA"
                : aggregationSummary.getReliabilityLevel().toUpperCase();

        if (weatherStatus.contains("tormenta")) {
            return List.of(
                    buildPlan(
                            "Plan de interior cercano",
                            "INTERIOR",
                            "Recomendado por posible tormenta o condiciones inestables.",
                            catalog.cinemaName,
                            catalog.cinemaAddress
                    ),
                    buildPlan(
                            "Cafetería o bar tranquilo",
                            "OCIO",
                            "Opción segura para mantener el plan sin depender del tiempo exterior.",
                            catalog.cafeName,
                            catalog.cafeAddress
                    ),
                    buildPlan(
                            "Actividad cultural cubierta",
                            "CULTURA",
                            "Alternativa adecuada para evitar exposición al mal tiempo.",
                            catalog.cultureName,
                            catalog.cultureAddress
                    )
            );
        }

        if (precipitationProbability >= 60 || weatherStatus.contains("lluvia") || weatherStatus.contains("llovizna")) {
            return List.of(
                    buildPlan(
                            "Cine o cartelera local",
                            "INTERIOR",
                            "Plan recomendado por probabilidad de lluvia o presencia de precipitación.",
                            catalog.cinemaName,
                            catalog.cinemaAddress
                    ),
                    buildPlan(
                            "Museo o exposición",
                            "CULTURA",
                            "Actividad adecuada para aprovechar el día en un espacio cubierto.",
                            catalog.cultureName,
                            catalog.cultureAddress
                    ),
                    buildPlan(
                            "Cafetería o bar cercano",
                            "OCIO",
                            "Alternativa cómoda si no conviene realizar planes al aire libre.",
                            catalog.cafeName,
                            catalog.cafeAddress
                    )
            );
        }

        if (windSpeed >= 30) {
            return List.of(
                    buildPlan(
                            "Ruta urbana corta",
                            "MIXTO",
                            "Plan flexible con posibilidad de refugiarse en espacios interiores si aumenta el viento.",
                            catalog.walkName,
                            catalog.walkAddress
                    ),
                    buildPlan(
                            "Cafetería con paseo breve",
                            "MIXTO",
                            "Combina actividad exterior moderada con una alternativa cubierta.",
                            catalog.cafeName,
                            catalog.cafeAddress
                    ),
                    buildPlan(
                            "Actividad interior alternativa",
                            "INTERIOR",
                            "Recomendable si el viento resulta incómodo en zonas abiertas.",
                            catalog.cinemaName,
                            catalog.cinemaAddress
                    )
            );
        }

        if (temperature >= 30) {
            return List.of(
                    buildPlan(
                            "Paseo en zona sombreada",
                            "EXTERIOR",
                            "Actividad ligera recomendada evitando las horas centrales del día.",
                            catalog.walkName,
                            catalog.walkAddress
                    ),
                    buildPlan(
                            "Terraza o bar en zona céntrica",
                            "OCIO",
                            "Buena opción para aprovechar el buen tiempo sin exposición excesiva al calor.",
                            catalog.cafeName,
                            catalog.cafeAddress
                    ),
                    buildPlan(
                            "Actividad interior climatizada",
                            "INTERIOR",
                            "Alternativa segura si la temperatura resulta elevada.",
                            catalog.cinemaName,
                            catalog.cinemaAddress
                    )
            );
        }

        if ("BAJA".equals(reliabilityLevel) || "MEDIA".equals(reliabilityLevel)) {
            return List.of(
                    buildPlan(
                            "Paseo urbano flexible",
                            "MIXTO",
                            "Condiciones aparentemente favorables, manteniendo una alternativa cubierta por fiabilidad no alta.",
                            catalog.walkName,
                            catalog.walkAddress
                    ),
                    buildPlan(
                            "Plan de ocio cercano",
                            "MIXTO",
                            "Opción adecuada para poder cambiar de plan si la previsión varía.",
                            catalog.cafeName,
                            catalog.cafeAddress
                    ),
                    buildPlan(
                            "Cartelera o actividad cubierta de respaldo",
                            "INTERIOR",
                            "Alternativa recomendada por incertidumbre entre proveedores.",
                            catalog.cinemaName,
                            catalog.cinemaAddress
                    )
            );
        }

        return List.of(
                buildPlan(
                        "Paseo al aire libre",
                        "EXTERIOR",
                        "Condiciones adecuadas para caminar o realizar actividad moderada.",
                        catalog.walkName,
                        catalog.walkAddress
                ),
                buildPlan(
                        "Bar o cafetería local",
                        "OCIO",
                        "Plan recomendado para disfrutar de la zona con condiciones meteorológicas favorables.",
                        catalog.cafeName,
                        catalog.cafeAddress
                ),
                buildPlan(
                        "Actividad cultural cercana",
                        "CULTURA",
                        "Buena opción para combinar ocio urbano y actividad cubierta.",
                        catalog.cultureName,
                        catalog.cultureAddress
                )
        );
    }

    private PersonalizedPlanResponse buildPlan(
            String title,
            String category,
            String description,
            String placeName,
            String address
    ) {
        return new PersonalizedPlanResponse(
                title,
                category,
                description,
                placeName,
                address,
                buildSearchUrl(placeName + " " + address)
        );
    }

    private String buildSearchUrl(String query) {
        return "https://www.google.com/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
    }

    private LocalPlanCatalog resolveLocalPlanCatalog(String locationName) {
        String location = locationName == null ? "" : locationName.toLowerCase();

        if (location.contains("barakaldo") || location.contains("baracaldo")) {
            return new LocalPlanCatalog(
                    "Cinesa Max Ocio",
                    "Max Ocio, Barakaldo",
                    "BEC Bilbao Exhibition Centre",
                    "Ronda de Azkue, Barakaldo",
                    "Café Boulevard Barakaldo",
                    "Centro de Barakaldo",
                    "Parque de los Hermanos",
                    "Barakaldo"
            );
        }

        if (location.contains("bilbao")) {
            return new LocalPlanCatalog(
                    "Cinesa Zubiarte",
                    "Centro Comercial Zubiarte, Bilbao",
                    "Museo Guggenheim Bilbao",
                    "Abandoibarra Etorbidea, Bilbao",
                    "Café Iruña",
                    "Jardines de Albia, Bilbao",
                    "Paseo de Abandoibarra",
                    "Bilbao"
            );
        }

        if (location.contains("madrid")) {
            return new LocalPlanCatalog(
                    "Cines Callao",
                    "Plaza del Callao, Madrid",
                    "Museo Nacional del Prado",
                    "Paseo del Prado, Madrid",
                    "Café Comercial",
                    "Glorieta de Bilbao, Madrid",
                    "Parque de El Retiro",
                    "Madrid"
            );
        }

        if (location.contains("barcelona")) {
            return new LocalPlanCatalog(
                    "Cines Verdi",
                    "Carrer de Verdi, Barcelona",
                    "Museu Nacional d'Art de Catalunya",
                    "Parc de Montjuïc, Barcelona",
                    "Els Quatre Gats",
                    "Carrer de Montsió, Barcelona",
                    "Parc de la Ciutadella",
                    "Barcelona"
            );
        }

        if (location.contains("valencia")) {
            return new LocalPlanCatalog(
                    "Cines Lys",
                    "Passeig de Russafa, Valencia",
                    "Ciudad de las Artes y las Ciencias",
                    "Avinguda del Professor López Piñero, Valencia",
                    "Horchatería Santa Catalina",
                    "Plaça de Santa Caterina, Valencia",
                    "Jardín del Turia",
                    "Valencia"
            );
        }

        if (location.contains("sevilla")) {
            return new LocalPlanCatalog(
                    "Cines Nervión Plaza",
                    "Centro Comercial Nervión Plaza, Sevilla",
                    "Museo de Bellas Artes de Sevilla",
                    "Plaza del Museo, Sevilla",
                    "Bar El Comercio",
                    "Calle Lineros, Sevilla",
                    "Plaza de España",
                    "Sevilla"
            );
        }

        if (location.contains("santander")) {
            return new LocalPlanCatalog(
                    "Cinesa Bahía de Santander",
                    "Centro Comercial Bahía de Santander",
                    "Centro Botín",
                    "Muelle de Albareda, Santander",
                    "Café Suizo",
                    "Centro de Santander",
                    "Paseo del Sardinero",
                    "Santander"
            );
        }

        return new LocalPlanCatalog(
                "Cine local",
                locationName == null ? "Zona consultada" : locationName,
                "Museo o centro cultural cercano",
                locationName == null ? "Zona consultada" : locationName,
                "Cafetería o bar cercano",
                locationName == null ? "Zona consultada" : locationName,
                "Ruta urbana cercana",
                locationName == null ? "Zona consultada" : locationName
        );
    }

    private static class LocalPlanCatalog {

        private final String cinemaName;
        private final String cinemaAddress;
        private final String cultureName;
        private final String cultureAddress;
        private final String cafeName;
        private final String cafeAddress;
        private final String walkName;
        private final String walkAddress;

        private LocalPlanCatalog(
                String cinemaName,
                String cinemaAddress,
                String cultureName,
                String cultureAddress,
                String cafeName,
                String cafeAddress,
                String walkName,
                String walkAddress
        ) {
            this.cinemaName = cinemaName;
            this.cinemaAddress = cinemaAddress;
            this.cultureName = cultureName;
            this.cultureAddress = cultureAddress;
            this.cafeName = cafeName;
            this.cafeAddress = cafeAddress;
            this.walkName = walkName;
            this.walkAddress = walkAddress;
        }
    }
}