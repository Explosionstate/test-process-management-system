package com.example.tracker.domain;

public record TestTaskRequest(Long planId, Long caseId, String title, Long assigneeId, String status, String dueDate) {
}
