package com.example.f1telemetryservice.controller;

import com.example.f1telemetryservice.dto.TelemetryDTO;
import com.example.f1telemetryservice.dto.TelemetrySnapshotDTO;
import com.example.f1telemetryservice.service.TelemetryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/telemetry")
@RequiredArgsConstructor
public class TelemetryController {

    private final TelemetryQueryService queryService;

    @GetMapping("/snapshot/{sessionKey}")
    public ResponseEntity<TelemetrySnapshotDTO> getSnapshot(@PathVariable Integer sessionKey) {
        return ResponseEntity.ok(queryService.getLatestSnapshot(sessionKey));
    }

    @GetMapping("/{sessionKey}/driver/{driverNumber}")
    public ResponseEntity<List<TelemetryDTO>> getDriverTelemetry(
            @PathVariable Integer sessionKey,
            @PathVariable Integer driverNumber) {
        return ResponseEntity.ok(queryService.getDriverTelemetry(sessionKey, driverNumber));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("telemetry-service UP");
    }
}
