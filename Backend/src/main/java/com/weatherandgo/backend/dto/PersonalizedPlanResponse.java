package com.weatherandgo.backend.dto;

public class PersonalizedPlanResponse {

    private String title;
    private String category;
    private String description;
    private String placeName;
    private String address;
    private String externalUrl;

    public PersonalizedPlanResponse() {
    }

    public PersonalizedPlanResponse(String title, String category, String description) {
        this.title = title;
        this.category = category;
        this.description = description;
    }

    public PersonalizedPlanResponse(
            String title,
            String category,
            String description,
            String placeName,
            String address,
            String externalUrl
    ) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.placeName = placeName;
        this.address = address;
        this.externalUrl = externalUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }
}