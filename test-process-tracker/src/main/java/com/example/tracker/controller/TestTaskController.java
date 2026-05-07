package com.example.tracker.controller;

import com.example.tracker.auth.AuthService;
import com.example.tracker.common.ApiResponse;
import com.example.tracker.domain.TestTaskRequest;
import com.example.tracker.service.TestTaskService;
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
@RequestMapping("/api/tasks")
public class TestTaskController {
    private final TestTaskService service;
    private final AuthService auth;

    public TestTaskController(TestTaskService service, AuthService auth) {
        this.service = service;
        this.auth = auth;
    }

    @GetMapping
    ApiResponse<List<Map<String, Object>>> list(@RequestParam(required = false) Long assigneeId,
                                                @RequestParam(required = false) String status,
                                                HttpSession session) {
        auth.require(session, "task:view");
        return ApiResponse.ok(service.list(assigneeId, status));
    }

    @PostMapping
    ApiResponse<Map<String, Object>> create(@RequestBody TestTaskRequest request, HttpSession session) {
        auth.require(session, "task:assign");
        return ApiResponse.ok(service.create(request));
    }

    @PutMapping("/{id}/status")
    ApiResponse<Void> status(@PathVariable Long id, @RequestBody TestTaskRequest request, HttpSession session) {
        auth.require(session, "task:update");
        service.updateStatus(id, request);
        return ApiResponse.ok(null);
    }
}
