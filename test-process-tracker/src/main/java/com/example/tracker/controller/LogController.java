package com.example.tracker.controller;

import com.example.tracker.auth.AuthService;
import com.example.tracker.common.ApiResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
public class LogController {
    private static final Logger log = LoggerFactory.getLogger(LogController.class);
    private static final Path LOG_FILE = Path.of("logs", "test-process-tracker.log");
    private final AuthService auth;

    public LogController(AuthService auth) {
        this.auth = auth;
    }

    @GetMapping
    ApiResponse<Map<String, Object>> latest(@RequestParam(defaultValue = "200") int lines, HttpSession session) throws IOException {
        auth.require(session, "user:manage");
        int safeLines = Math.max(20, Math.min(lines, 1000));
        List<String> content = Files.exists(LOG_FILE) ? Files.readAllLines(LOG_FILE, StandardCharsets.UTF_8) : List.of();
        int from = Math.max(0, content.size() - safeLines);
        log.info("log_preview lines={} returned={}", safeLines, content.size() - from);
        return ApiResponse.ok(Map.of(
                "file", LOG_FILE.toString(),
                "totalLines", content.size(),
                "lines", content.subList(from, content.size())
        ));
    }

    @GetMapping("/export")
    ResponseEntity<byte[]> export(HttpSession session) throws IOException {
        auth.require(session, "user:manage");
        byte[] bytes = Files.exists(LOG_FILE) ? Files.readAllBytes(LOG_FILE) : new byte[0];
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        log.info("log_exported file={} bytes={}", LOG_FILE, bytes.length);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("system-log-" + timestamp + ".log").build().toString())
                .body(bytes);
    }
}
