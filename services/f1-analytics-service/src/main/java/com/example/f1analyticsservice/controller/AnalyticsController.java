package com.example.f1analyticsservice.controller;

import com.example.f1analyticsservice.dto.DriverStatsDTO;
import com.example.f1analyticsservice.dto.SessionSummaryDTO;
import com.example.f1analyticsservice.service.AnalyticsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsQueryService queryService;

    @GetMapping("/session/{sessionKey}/drivers")
    public ResponseEntity<List<DriverStatsDTO>> getDriverStats(@PathVariable Integer sessionKey) {
        return ResponseEntity.ok(queryService.getSessionDriverStats(sessionKey));
    }

    @GetMapping("/session/{sessionKey}/driver/{driverNumber}")
    public ResponseEntity<DriverStatsDTO> getSingleDriverStats(
            @PathVariable Integer sessionKey,
            @PathVariable Integer driverNumber) {
        return ResponseEntity.ok(queryService.getDriverStats(sessionKey, driverNumber));
    }

    @GetMapping("/session/{sessionKey}/summary")
    public ResponseEntity<SessionSummaryDTO> getSessionSummary(@PathVariable Integer sessionKey) {
        return ResponseEntity.ok(queryService.getSessionSummary(sessionKey));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("analytics-service UP");
    }
}
