package com.example.tracker.auth;

import com.example.tracker.common.ApiResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
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
        Object user = session.getAttribute(AuthService.SESSION_USER);
        log.info("logout user={}", user);
        session.invalidate();
        return ApiResponse.ok(null);
    }

    @GetMapping("/me")
    ApiResponse<LoginUser> me(HttpSession session) {
        return ApiResponse.ok(authService.current(session));
    }
}
