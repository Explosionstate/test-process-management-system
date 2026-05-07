package com.example.tracker.controller;

import com.example.tracker.auth.AuthService;
import com.example.tracker.common.ApiResponse;
import com.example.tracker.domain.TestCaseRequest;
import com.example.tracker.service.TestCaseService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cases")
public class TestCaseController {
    private final TestCaseService service;
    private final AuthService auth;

    public TestCaseController(TestCaseService service, AuthService auth) {
        this.service = service;
        this.auth = auth;
    }

    @GetMapping
    ApiResponse<List<Map<String, Object>>> list(@RequestParam(required = false) Long planId,
                                                @RequestParam(required = false) String result,
                                                HttpSession session) {
        auth.require(session, "case:view");
        return ApiResponse.ok(service.list(planId, result));
    }

    @PostMapping
    ApiResponse<Map<String, Object>> create(@RequestBody TestCaseRequest request, HttpSession session) {
        auth.require(session, "case:create");
        return ApiResponse.ok(service.create(request));
    }

    @PutMapping("/{id}/execute")
    ApiResponse<Void> execute(@PathVariable Long id, @RequestBody TestCaseRequest request, HttpSession session) {
        auth.require(session, "case:execute");
        service.execute(id, request);
        return ApiResponse.ok(null);
    }
}
