package com.example.f1timingservice.controller;


import com.example.f1timingservice.dto.response.LapTimeResponseDTO;
import com.example.f1timingservice.dto.response.StintResponseDTO;
import com.example.f1timingservice.mapper.TimingMapper;
import com.example.f1timingservice.model.LapTime;
import com.example.f1timingservice.service.TimingQueryService;
import com.example.f1timingservice.service.TimingSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/timing")
@RequiredArgsConstructor
public class TimingController {

    private final TimingQueryService queryService;
    private final TimingSyncService syncService;
    private final TimingMapper mapper;

    // Lap sync
    @PostMapping("/sync/laps/{sessionKey}")
    public ResponseEntity<String> syncLaps(@PathVariable Integer sessionKey) {
        syncService.syncLapsBySession(sessionKey);
        return ResponseEntity.ok("Lap sync started - session: " + sessionKey);
    }

    // Stint sync
    @PostMapping("/sync/stints/{sessionKey}")
    public ResponseEntity<String> syncStints(@PathVariable Integer sessionKey) {
        syncService.syncStintsBySession(sessionKey);
        return ResponseEntity.ok("Stint sync started- session: " + sessionKey);
    }

    @PostMapping("/sync/session/{sessionKey}")
    public ResponseEntity<String> syncFullSession(@PathVariable Integer sessionKey) {
        CompletableFuture<Void> future = syncService.syncSessionAsync(sessionKey);
        return ResponseEntity.accepted().body("Session sync started - session: " + sessionKey);
    }

    // Sessionun bütün lapları
    @GetMapping("/laps/{sessionKey}")
    public ResponseEntity<List<LapTimeResponseDTO>> getLaps(@PathVariable Integer sessionKey) {
        return ResponseEntity.ok(
                queryService.getLapsBySession(sessionKey).stream()
                        .map(mapper::toLapDTO)
                        .toList()
        );
    }

    @GetMapping("/laps/{sessionKey}/paginated")
    public ResponseEntity<Page<LapTime>> getLapsPaginated(
            @PathVariable Integer sessionKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ){
        return ResponseEntity.ok(queryService.getLapsBySessionPaginated(sessionKey, page, size));
    }

    // Sürücünün lapları
    @GetMapping("/laps/{sessionKey}/driver/{driverNumber}")
    public ResponseEntity<List<LapTimeResponseDTO>> getDriverLaps(
            @PathVariable Integer sessionKey,
            @PathVariable Integer driverNumber) {
        return ResponseEntity.ok(
                queryService.getDriverLaps(sessionKey, driverNumber).stream()
                        .map(mapper::toLapDTO)
                        .toList()
        );
    }

    // Ən sürətli laplar
    @GetMapping("/laps/{sessionKey}/fastest")
    public ResponseEntity<List<LapTimeResponseDTO>> getFastestLaps(@PathVariable Integer sessionKey) {
        return ResponseEntity.ok(
                queryService.getFastestLaps(sessionKey).stream()
                        .map(mapper::toLapDTO)
                        .toList()
        );
    }

    // Sessionun stintləri
    @GetMapping("/stints/{sessionKey}")
    public ResponseEntity<List<StintResponseDTO>> getStints(@PathVariable Integer sessionKey) {
        return ResponseEntity.ok(
                queryService.getStintsBySession(sessionKey).stream()
                        .map(mapper::toStintDTO)
                        .toList()
        );
    }

    // Sürücünün stintləri
    @GetMapping("/stints/{sessionKey}/driver/{driverNumber}")
    public ResponseEntity<List<StintResponseDTO>> getDriverStints(
            @PathVariable Integer sessionKey,
            @PathVariable Integer driverNumber) {
        return ResponseEntity.ok(
                queryService.getDriverStints(sessionKey, driverNumber).stream()
                        .map(mapper::toStintDTO)
                        .toList()
        );
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("timing-service UP");
    }
}
