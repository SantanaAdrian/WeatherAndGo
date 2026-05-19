package com.weatherandgo.backend.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class LocationService {

    private final RestClient restClient;

    public LocationService() {
        this.restClient = RestClient.builder()
                .defaultHeader("User-Agent", "MeteoWeb-TFE/1.0 santana.txiki@gmail.com")
                .build();
    }

    public String resolveLocationName(Double latitude, Double longitude) {
        try {
            String url = "https://nominatim.openstreetmap.org/reverse"
                    + "?format=jsonv2"
                    + "&lat=" + latitude
                    + "&lon=" + longitude
                    + "&zoom=10"
                    + "&addressdetails=1"
                    + "&accept-language=es";

            Map<String, Object> response = restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (response == null || !response.containsKey("address")) {
                return "Ubicación seleccionada";
            }

            Object addressObject = response.get("address");

            if (!(addressObject instanceof Map<?, ?> address)) {
                return "Ubicación seleccionada";
            }

            String city = getAddressValue(address, "city");
            String town = getAddressValue(address, "town");
            String village = getAddressValue(address, "village");
            String municipality = getAddressValue(address, "municipality");
            String suburb = getAddressValue(address, "suburb");
            String state = getAddressValue(address, "state");

            String locationName = firstNonBlank(city, town, village, municipality, suburb);

            if (locationName != null && state != null) {
                return locationName + ", " + state;
            }

            if (locationName != null) {
                return locationName;
            }

            Object displayName = response.get("display_name");

            if (displayName != null) {
                return displayName.toString();
            }

            return "Ubicación seleccionada";

        } catch (Exception exception) {
            return "Ubicación seleccionada";
        }
    }

    private String getAddressValue(Map<?, ?> address, String key) {
        Object value = address.get(key);

        if (value == null || value.toString().isBlank()) {
            return null;
        }

        return value.toString();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }
}