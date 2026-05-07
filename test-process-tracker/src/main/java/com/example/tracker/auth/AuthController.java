package com.example.tracker.auth;

import com.example.tracker.common.ApiResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    ApiResponse<LoginUser> login(@RequestBody LoginRequest request, HttpSession session) {
        return ApiResponse.ok(authService.login(request, session));
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(HttpSession session) {
        session.invalidate();
        return ApiResponse.ok(null);
    }

    @GetMapping("/me")
    ApiResponse<LoginUser> me(HttpSession session) {
        return ApiResponse.ok(authService.current(session));
    }
}
