package com.example.f1telemetryservice;

import com.example.f1telemetryservice.controller.TelemetryController;
import com.example.f1telemetryservice.dto.PositionDTO;
import com.example.f1telemetryservice.dto.TelemetryDTO;
import com.example.f1telemetryservice.dto.TelemetrySnapshotDTO;
import com.example.f1telemetryservice.service.TelemetryQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TelemetryController Tests")
class TelemetryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TelemetryQueryService queryService;

    @InjectMocks
    private TelemetryController controller;

    private static final Integer SESSION_KEY = 9158;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /snapshot/{sessionKey} - returns telemetry snapshot")
    void getSnapshot_existingSession_returnsSnapshot() throws Exception {
        TelemetrySnapshotDTO snapshot = TelemetrySnapshotDTO.builder()
                .sessionKey(SESSION_KEY)
                .drivers(List.of(
                        TelemetrySnapshotDTO.DriverTelemetryDTO.builder()
                                .driverNumber(1)
                                .speed(285.5)
                                .gear(7)
                                .throttle(100.0)
                                .drs(1.0)
                                .position(PositionDTO.builder().x(100.0).y(200.0).z(0.0).lapDistance(1500.0).build())
                                .build(),
                        TelemetrySnapshotDTO.DriverTelemetryDTO.builder()
                                .driverNumber(44)
                                .speed(290.2)
                                .gear(7)
                                .throttle(98.5)
                                .drs(1.0)
                                .position(PositionDTO.builder().x(105.0).y(205.0).z(0.0).lapDistance(1520.0).build())
                                .build()
                ))
                .build();

        when(queryService.getLatestSnapshot(SESSION_KEY)).thenReturn(snapshot);

        mockMvc.perform(get("/api/telemetry/snapshot/{sessionKey}", SESSION_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionKey", is(SESSION_KEY)))
                .andExpect(jsonPath("$.drivers", hasSize(2)))
                .andExpect(jsonPath("$.drivers[0].driverNumber", is(1)))
                .andExpect(jsonPath("$.drivers[0].speed", is(285.5)))
                .andExpect(jsonPath("$.drivers[0].position.lapDistance", is(1500.0)))
                .andExpect(jsonPath("$.drivers[1].driverNumber", is(44)))
                .andExpect(jsonPath("$.drivers[1].position.x", is(105.0)));
    }

    @Test
    @DisplayName("GET /snapshot/{sessionKey} - empty session returns empty drivers")
    void getSnapshot_emptySession_returnsEmpty() throws Exception {
        TelemetrySnapshotDTO snapshot = TelemetrySnapshotDTO.builder()
                .sessionKey(SESSION_KEY)
                .drivers(List.of())
                .build();

        when(queryService.getLatestSnapshot(SESSION_KEY)).thenReturn(snapshot);

        mockMvc.perform(get("/api/telemetry/snapshot/{sessionKey}", SESSION_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.drivers", hasSize(0)));
    }

    @Test
    @DisplayName("GET /{sessionKey}/driver/{driverNumber} - returns driver telemetry")
    void getDriverTelemetry_existingDriver_returnsTelemetry() throws Exception {
        List<TelemetryDTO> telemetry = List.of(
                TelemetryDTO.builder()
                        .sessionKey(SESSION_KEY)
                        .driverNumber(1)
                        .speed(280.0)
                        .rpm(10500)
                        .gear(7)
                        .throttle(100.0)
                        .brake(0.0)
                        .drs(1.0)
                        .timestamp(LocalDateTime.now())
                        .build(),
                TelemetryDTO.builder()
                        .sessionKey(SESSION_KEY)
                        .driverNumber(1)
                        .speed(285.0)
                        .rpm(10800)
                        .gear(7)
                        .throttle(100.0)
                        .brake(0.0)
                        .drs(1.0)
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        when(queryService.getDriverTelemetry(SESSION_KEY, 1)).thenReturn(telemetry);

        mockMvc.perform(get("/api/telemetry/{sessionKey}/driver/{driverNumber}", SESSION_KEY, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].driverNumber", is(1)))
                .andExpect(jsonPath("$[0].speed", is(280.0)))
                .andExpect(jsonPath("$[0].rpm", is(10500)))
                .andExpect(jsonPath("$[1].speed", is(285.0)));
    }

    @Test
    @DisplayName("GET /{sessionKey}/driver/{driverNumber} - non-existing driver returns empty")
    void getDriverTelemetry_nonExisting_returnsEmpty() throws Exception {
        when(queryService.getDriverTelemetry(SESSION_KEY, 999)).thenReturn(List.of());

        mockMvc.perform(get("/api/telemetry/{sessionKey}/driver/{driverNumber}", SESSION_KEY, 999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /health - returns UP")
    void healthCheck_returnsUp() throws Exception {
        mockMvc.perform(get("/api/telemetry/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("telemetry-service UP"));
    }
}
