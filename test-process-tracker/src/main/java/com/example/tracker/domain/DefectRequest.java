package com.example.tracker.domain;

public record DefectRequest(String title, String module, String severity, String priority, String steps,
                            String expected, String actual, Long ownerId) {
}
