package com.weatherandgo.backend.client;

import com.weatherandgo.backend.model.NormalizedWeatherData;

public interface WeatherProviderClient {

    String getProviderName();

    NormalizedWeatherData getForecast(Double latitude, Double longitude);
}