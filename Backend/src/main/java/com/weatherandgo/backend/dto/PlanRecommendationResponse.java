package com.weatherandgo.backend.dto;

import java.util.List;

public class PlanRecommendationResponse {

    private String category;
    private Integer confidence;
    private String summary;
    private List<String> recommendedPlanTypes;
    private List<String> reasons;
    private List<PersonalizedPlanResponse> personalizedPlans;

    public PlanRecommendationResponse() {
    }

    public PlanRecommendationResponse(
            String category,
            Integer confidence,
            String summary,
            List<String> recommendedPlanTypes,
            List<String> reasons
    ) {
        this.category = category;
        this.confidence = confidence;
        this.summary = summary;
        this.recommendedPlanTypes = recommendedPlanTypes;
        this.reasons = reasons;
    }

    public PlanRecommendationResponse(
            String category,
            Integer confidence,
            String summary,
            List<String> recommendedPlanTypes,
            List<String> reasons,
            List<PersonalizedPlanResponse> personalizedPlans
    ) {
        this.category = category;
        this.confidence = confidence;
        this.summary = summary;
        this.recommendedPlanTypes = recommendedPlanTypes;
        this.reasons = reasons;
        this.personalizedPlans = personalizedPlans;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getConfidence() {
        return confidence;
    }

    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getRecommendedPlanTypes() {
        return recommendedPlanTypes;
    }

    public void setRecommendedPlanTypes(List<String> recommendedPlanTypes) {
        this.recommendedPlanTypes = recommendedPlanTypes;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public List<PersonalizedPlanResponse> getPersonalizedPlans() {
        return personalizedPlans;
    }

    public void setPersonalizedPlans(List<PersonalizedPlanResponse> personalizedPlans) {
        this.personalizedPlans = personalizedPlans;
    }
}