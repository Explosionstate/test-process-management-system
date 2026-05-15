package com.example.tracker.controller;

import com.example.tracker.auth.AuthService;
import com.example.tracker.common.ApiResponse;
import com.example.tracker.service.DashboardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DashboardController {
    private final DashboardService service;
    private final AuthService auth;

    public DashboardController(DashboardService service, AuthService auth) {
        this.service = service;
        this.auth = auth;
    }

    @GetMapping("/dashboard")
    ApiResponse<Map<String, Object>> dashboard(HttpSession session) {
        auth.require(session, "report:view");
        return ApiResponse.ok(service.dashboard());
    }

    @GetMapping("/report")
    ApiResponse<Map<String, Object>> report(@RequestParam(defaultValue = "1") Long planId, HttpSession session) {
        auth.require(session, "report:view");
        return ApiResponse.ok(service.report(planId));
    }

    @GetMapping("/report/export")
    ResponseEntity<byte[]> export(@RequestParam(defaultValue = "1") Long planId,
                                  @RequestParam(defaultValue = "word") String format,
                                  HttpSession session) {
        auth.require(session, "report:create");
        boolean pdf = "pdf".equalsIgnoreCase(format);
        byte[] bytes = pdf ? service.exportPdf(planId) : service.exportWord(planId);
        String filename = pdf ? "test-report.pdf" : "test-report.doc";
        MediaType mediaType = pdf ? MediaType.APPLICATION_PDF : MediaType.parseMediaType("application/msword");
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .body(bytes);
    }
}
