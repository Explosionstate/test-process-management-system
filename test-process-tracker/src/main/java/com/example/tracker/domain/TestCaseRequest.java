package com.example.tracker.domain;

public record TestCaseRequest(Long planId, String module, String title, String precondition, String steps,
                              String expected, String actual, String result, Long executorId) {
}
