package com.example.tracker.domain;

public record UserRequest(String username, String password, String realName, Long roleId, Boolean enabled) {
}
