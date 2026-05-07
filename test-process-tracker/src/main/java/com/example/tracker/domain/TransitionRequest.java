package com.example.tracker.domain;

public record TransitionRequest(String status, Long ownerId, String note) {
}
