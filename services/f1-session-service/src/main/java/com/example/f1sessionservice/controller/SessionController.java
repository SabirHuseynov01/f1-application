package com.example.f1sessionservice.controller;

import com.example.f1sessionservice.dto.response.SyncResultDTO;
import com.example.f1sessionservice.dto.response.ApiResponse;
import com.example.f1sessionservice.dto.response.DriverResponseDTO;
import com.example.f1sessionservice.dto.response.SeasonResponseDTO;
import com.example.f1sessionservice.dto.response.SessionResponseDTO;
import com.example.f1sessionservice.mapper.SessionMapper;
import com.example.f1sessionservice.model.SessionType;
import com.example.f1sessionservice.service.CircuitSyncService;
import com.example.f1sessionservice.service.SessionQueryService;
import com.example.f1sessionservice.service.SessionSyncService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Validated
@Slf4j
public class SessionController extends BaseController {

    private final SessionQueryService queryService;
    private final SessionSyncService syncService;
    private final SessionMapper mapper;
    private final CircuitSyncService circuitSyncService;

    // Bütün seasonlar - Session resource altında
    @GetMapping("/seasons")
    public ResponseEntity<ApiResponse<List<SeasonResponseDTO>>> getAllSeasons() {
        return ok(queryService.getAllSeasons().stream()
                .map(mapper::toSeasonDTO)
                .toList());
    }

    // İlə görə sessionlar
    @GetMapping("/year/{year}")
    public ResponseEntity<ApiResponse<List<SessionResponseDTO>>> getByYear(
            @PathVariable @Min(1950) Integer year) {
        return ok(queryService.getSessionsByYear(year).stream()
                .map(mapper::toSessionDTO)
                .toList());
    }

    // İl + tip filtri
    @GetMapping("/year/{year}/type/{type}")
    public ResponseEntity<ApiResponse<List<SessionResponseDTO>>> getByYearAndType(
            @PathVariable @Min(1950) Integer year,
            @PathVariable SessionType type) {
        return ok(queryService.getSessionsByYearAndType(year, type).stream()
                .map(mapper::toSessionDTO)
                .toList());
    }

    // Session key ilə tək session
    @GetMapping("/key/{sessionKey}")
    public ResponseEntity<ApiResponse<SessionResponseDTO>> getByKey(@PathVariable Integer sessionKey) {
        return ok(mapper.toSessionDTO(queryService.getSessionByKey(sessionKey)));
    }

    // Sessionun sürücüləri
    @GetMapping("/{sessionId}/drivers")
    public ResponseEntity<ApiResponse<List<DriverResponseDTO>>> getDrivers(@PathVariable Long sessionId) {
        return ok(queryService.getDriversBySession(sessionId).stream()
                .map(mapper::toDriverDTO)
                .toList());
    }

    @GetMapping("/drivers/unique")
    public ResponseEntity<ApiResponse<List<DriverResponseDTO>>> getUniqueDrivers() {
        List<DriverResponseDTO> uniqueDrivers = queryService.getAllDrivers().stream()
                .collect(Collectors.toMap(
                        d -> d.getDriverCode(),
                        d -> mapper.toDriverDTO(d),
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .toList();

        return ok(uniqueDrivers);
    }

    @PostMapping("/sync/{year}")
    public ResponseEntity<ApiResponse<SyncResultDTO>> syncYear(@PathVariable @Min(1950) Integer year) {
        log.info(">>> SYNC REQUEST RECEIVED FOR YEAR: {} <<<", year);
        SyncResultDTO result = syncService.syncByYear(year);
        log.info(">>> SYNC COMPLETED WITH RESULT: {} <<<", result);
        return ok(result);
    }

    @PostMapping("/sync/drivers/{sessionKey}")
    public ResponseEntity<ApiResponse<SyncResultDTO>> syncDrivers(@PathVariable Integer sessionKey) {
        SyncResultDTO result = syncService.syncDriversForSessionKey(sessionKey);
        return ok(result);
    }

    @PostMapping("/sync/circuits/enrich")
    public ResponseEntity<ApiResponse<SyncResultDTO>> enrichAllCircuits() {
        circuitSyncService.enrichAllCircuits();
        return ok(SyncResultDTO.builder()
                .status("CIRCUITS_ENRICHED")
                .build());
    }
}
