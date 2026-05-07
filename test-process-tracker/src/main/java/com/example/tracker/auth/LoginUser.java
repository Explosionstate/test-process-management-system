package com.example.tracker.auth;

import java.util.List;

public record LoginUser(Long id, String username, String realName, List<String> roles, List<String> permissions) {
    public boolean has(String permission) {
        return permissions.contains(permission);
    }
}
