package com.example.tracker.controller;

import com.example.tracker.auth.AuthService;
import com.example.tracker.common.ApiResponse;
import com.example.tracker.domain.DefectRequest;
import com.example.tracker.domain.TransitionRequest;
import com.example.tracker.service.DefectService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/defects")
public class DefectController {
    private final DefectService service;
    private final AuthService auth;

    public DefectController(DefectService service, AuthService auth) {
        this.service = service;
        this.auth = auth;
    }

    @GetMapping
    ApiResponse<List<Map<String, Object>>> list(@RequestParam(required = false) String status,
                                                @RequestParam(required = false) String module,
                                                @RequestParam(required = false) Long ownerId,
                                                HttpSession session) {
        auth.require(session, "defect:view");
        return ApiResponse.ok(service.list(status, module, ownerId));
    }

    @PostMapping
    ApiResponse<Map<String, Object>> create(@RequestBody DefectRequest request, HttpSession session) {
        auth.require(session, "defect:create");
        return ApiResponse.ok(service.create(request, auth.current(session)));
    }

    @PostMapping("/{id}/transition")
    ApiResponse<Void> transition(@PathVariable Long id, @RequestBody TransitionRequest request, HttpSession session) {
        service.transition(id, request, auth.current(session));
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}/history")
    ApiResponse<List<Map<String, Object>>> history(@PathVariable Long id, HttpSession session) {
        auth.require(session, "defect:view");
        return ApiResponse.ok(service.history(id));
    }
}
