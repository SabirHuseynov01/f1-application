package com.example.f1apigateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
public class FallbackController {

    @GetMapping("/fallback/session")
    public ResponseEntity<Map<String, Object>> sessionFallback() {
        log.warn("Session service fallback triggered");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "message", "Session service is temporarily unavailable. Please try again later.",
                        "timestamp", LocalDateTime.now()
                ));
    }

    @GetMapping("/fallback/timing")
    public ResponseEntity<Map<String, Object>> timingFallback() {
        log.warn("Timing service fallback triggered");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "message", "Timing service is temporarily unavailable. Please try again later.",
                        "timestamp", LocalDateTime.now()
                ));
    }
}
