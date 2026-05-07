package com.example.tracker.domain;

public record TestPlanRequest(String name, String objective, String scopeText, Long ownerId, String status,
                              String startDate, String endDate) {
}
