package com.example.f1sessionservice.controller;

import com.example.f1sessionservice.dto.response.ApiResponse;
import com.example.f1sessionservice.dto.response.CircuitResponseDTO;
import com.example.f1sessionservice.dto.response.SyncResultDTO;
import com.example.f1sessionservice.service.CircuitSyncService;
import com.example.f1sessionservice.service.SessionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/circuits")
@RequiredArgsConstructor
public class CircuitController extends BaseController {

    private final SessionQueryService queryService;
    private final CircuitSyncService circuitSyncService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CircuitResponseDTO>>> getAllCircuits() {
        List<CircuitResponseDTO> circuits = queryService.getAllCircuits().stream()
                .map(c -> CircuitResponseDTO.builder()
                        .id(c.getId())
                        .circuitKey(c.getCircuitKey())
                        .name(c.getName())
                        .country(c.getCountry())
                        .city(c.getCity())
                        .trackLengthKm(c.getTrackLengthKm())
                        .corners(c.getCorners())
                        .build())
                .toList();
        return ok(circuits);
    }

    @GetMapping("/{circuitKey}")
    public ResponseEntity<ApiResponse<CircuitResponseDTO>> getCircuitByKey(@PathVariable Integer circuitKey) {
        var circuit = queryService.getAllCircuits().stream()
                .filter(c -> c.getCircuitKey().equals(circuitKey))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Circuit not found: " + circuitKey));

        return ok(CircuitResponseDTO.builder()
                .id(circuit.getId())
                .circuitKey(circuit.getCircuitKey())
                .name(circuit.getName())
                .country(circuit.getCountry())
                .city(circuit.getCity())
                .trackLengthKm(circuit.getTrackLengthKm())
                .corners(circuit.getCorners())
                .build());
    }

    @PostMapping("/enrich/{circuitKey}")
    public ResponseEntity<ApiResponse<CircuitResponseDTO>> enrichCircuit(@PathVariable Integer circuitKey) {
        circuitSyncService.enrichCircuitDetails(circuitKey);

        var circuit = queryService.getAllCircuits().stream()
                .filter(c -> c.getCircuitKey().equals(circuitKey))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Circuit not found: " + circuitKey));

        return ok(CircuitResponseDTO.builder()
                .id(circuit.getId())
                .circuitKey(circuit.getCircuitKey())
                .name(circuit.getName())
                .country(circuit.getCountry())
                .city(circuit.getCity())
                .trackLengthKm(circuit.getTrackLengthKm())
                .corners(circuit.getCorners())
                .build());
    }

    @PostMapping("/enrich-all")
    public ResponseEntity<ApiResponse<SyncResultDTO>> enrichAllCircuits() {
        circuitSyncService.enrichAllCircuits();
        return ok(SyncResultDTO.builder()
                .status("ALL_CIRCUITS_ENRICHED")
                .build());
    }
}
