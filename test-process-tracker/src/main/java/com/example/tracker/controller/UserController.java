package com.example.tracker.controller;

import com.example.tracker.auth.AuthService;
import com.example.tracker.common.ApiResponse;
import com.example.tracker.domain.UserRequest;
import com.example.tracker.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping
    ApiResponse<List<Map<String, Object>>> users(HttpSession session) {
        authService.require(session, "user:manage");
        return ApiResponse.ok(userService.users());
    }

    @PostMapping
    ApiResponse<Map<String, Object>> create(@RequestBody UserRequest request, HttpSession session) {
        authService.require(session, "user:manage");
        return ApiResponse.ok(userService.create(request));
    }

    @GetMapping("/roles")
    ApiResponse<List<Map<String, Object>>> roles(HttpSession session) {
        authService.current(session);
        return ApiResponse.ok(userService.roles());
    }
}
