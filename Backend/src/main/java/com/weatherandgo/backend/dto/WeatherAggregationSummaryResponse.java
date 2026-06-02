package com.weatherandgo.backend.dto;

public class WeatherAggregationSummaryResponse {

    private Integer providerCount;
    private Double temperatureDifference;
    private Integer humidityDifference;
    private Double windSpeedDifference;
    private Integer precipitationDifference;
    private String reliabilityLevel;
    private String explanation;

    public WeatherAggregationSummaryResponse() {
    }

    public WeatherAggregationSummaryResponse(
            Integer providerCount,
            Double temperatureDifference,
            Integer humidityDifference,
            Double windSpeedDifference,
            Integer precipitationDifference,
            String reliabilityLevel,
            String explanation
    ) {
        this.providerCount = providerCount;
        this.temperatureDifference = temperatureDifference;
        this.humidityDifference = humidityDifference;
        this.windSpeedDifference = windSpeedDifference;
        this.precipitationDifference = precipitationDifference;
        this.reliabilityLevel = reliabilityLevel;
        this.explanation = explanation;
    }

    public Integer getProviderCount() {
        return providerCount;
    }

    public void setProviderCount(Integer providerCount) {
        this.providerCount = providerCount;
    }

    public Double getTemperatureDifference() {
        return temperatureDifference;
    }

    public void setTemperatureDifference(Double temperatureDifference) {
        this.temperatureDifference = temperatureDifference;
    }

    public Integer getHumidityDifference() {
        return humidityDifference;
    }

    public void setHumidityDifference(Integer humidityDifference) {
        this.humidityDifference = humidityDifference;
    }

    public Double getWindSpeedDifference() {
        return windSpeedDifference;
    }

    public void setWindSpeedDifference(Double windSpeedDifference) {
        this.windSpeedDifference = windSpeedDifference;
    }

    public Integer getPrecipitationDifference() {
        return precipitationDifference;
    }

    public void setPrecipitationDifference(Integer precipitationDifference) {
        this.precipitationDifference = precipitationDifference;
    }

    public String getReliabilityLevel() {
        return reliabilityLevel;
    }

    public void setReliabilityLevel(String reliabilityLevel) {
        this.reliabilityLevel = reliabilityLevel;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}