package com.example.tracker.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FaviconController {
    @GetMapping("/favicon.ico")
    ResponseEntity<byte[]> favicon() {
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/x-icon"))
                .body(new byte[0]);
    }
}
