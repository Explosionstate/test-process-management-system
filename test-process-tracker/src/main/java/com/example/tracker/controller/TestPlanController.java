package com.example.tracker.controller;

import com.example.tracker.auth.AuthService;
import com.example.tracker.common.ApiResponse;
import com.example.tracker.domain.TestPlanRequest;
import com.example.tracker.service.TestPlanService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
public class TestPlanController {
    private final TestPlanService service;
    private final AuthService auth;

    public TestPlanController(TestPlanService service, AuthService auth) {
        this.service = service;
        this.auth = auth;
    }

    @GetMapping
    ApiResponse<List<Map<String, Object>>> list(HttpSession session) {
        auth.require(session, "plan:view");
        return ApiResponse.ok(service.list());
    }

    @PostMapping
    ApiResponse<Map<String, Object>> create(@RequestBody TestPlanRequest request, HttpSession session) {
        auth.require(session, "plan:create");
        return ApiResponse.ok(service.create(request, auth.current(session)));
    }

    @PutMapping("/{id}")
    ApiResponse<Void> update(@PathVariable Long id, @RequestBody TestPlanRequest request, HttpSession session) {
        auth.require(session, "plan:update");
        service.update(id, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> delete(@PathVariable Long id, HttpSession session) {
        auth.require(session, "plan:update");
        service.delete(id);
        return ApiResponse.ok(null);
    }
}
