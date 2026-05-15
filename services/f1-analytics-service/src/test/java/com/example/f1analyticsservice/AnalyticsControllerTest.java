package com.example.f1analyticsservice;

import com.example.f1analyticsservice.controller.AnalyticsController;
import com.example.f1analyticsservice.dto.DriverStatsDTO;
import com.example.f1analyticsservice.dto.SessionSummaryDTO;
import com.example.f1analyticsservice.service.AnalyticsQueryService;
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
@DisplayName("AnalyticsController Tests")
class AnalyticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AnalyticsQueryService queryService;

    @InjectMocks
    private AnalyticsController controller;

    private static final Integer SESSION_KEY = 9158;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /session/{sessionKey}/drivers - returns driver stats")
    void getDriverStats_returnsStats() throws Exception {
        List<DriverStatsDTO> stats = List.of(
                DriverStatsDTO.builder()
                        .sessionKey(SESSION_KEY)
                        .driverNumber(1)
                        .totalLaps(57)
                        .fastestLapTime(92.456)
                        .avgLapTime(95.123)
                        .totalPitStops(2)
                        .maxSpeed(325.5)
                        .avgSpeed(210.3)
                        .calculatedAt(LocalDateTime.now())
                        .build(),
                DriverStatsDTO.builder()
                        .sessionKey(SESSION_KEY)
                        .driverNumber(44)
                        .totalLaps(57)
                        .fastestLapTime(91.892)
                        .avgLapTime(94.567)
                        .totalPitStops(2)
                        .maxSpeed(328.1)
                        .avgSpeed(212.5)
                        .calculatedAt(LocalDateTime.now())
                        .build()
        );

        when(queryService.getSessionDriverStats(SESSION_KEY)).thenReturn(stats);

        mockMvc.perform(get("/api/analytics/session/{sessionKey}/drivers", SESSION_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].driverNumber", is(1)))
                .andExpect(jsonPath("$[0].fastestLapTime", is(92.456)))
                .andExpect(jsonPath("$[1].driverNumber", is(44)))
                .andExpect(jsonPath("$[1].fastestLapTime", is(91.892)));
    }

    @Test
    @DisplayName("GET /session/{sessionKey}/driver/{driverNumber} - returns single driver stats")
    void getSingleDriverStats_returnsStats() throws Exception {
        DriverStatsDTO stats = DriverStatsDTO.builder()
                .sessionKey(SESSION_KEY)
                .driverNumber(1)
                .totalLaps(57)
                .fastestLapTime(92.456)
                .avgLapTime(95.123)
                .build();

        when(queryService.getDriverStats(SESSION_KEY, 1)).thenReturn(stats);

        mockMvc.perform(get("/api/analytics/session/{sessionKey}/driver/{driverNumber}", SESSION_KEY, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driverNumber", is(1)))
                .andExpect(jsonPath("$.totalLaps", is(57)));
    }

    @Test
    @DisplayName("GET /session/{sessionKey}/driver/{driverNumber} - null stats returns null")
    void getSingleDriverStats_null_returnsNull() throws Exception {
        when(queryService.getDriverStats(SESSION_KEY, 999)).thenReturn(null);

        mockMvc.perform(get("/api/analytics/session/{sessionKey}/driver/{driverNumber}", SESSION_KEY, 999))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("GET /session/{sessionKey}/summary - returns session summary")
    void getSessionSummary_returnsSummary() throws Exception {
        SessionSummaryDTO summary = SessionSummaryDTO.builder()
                .sessionKey(SESSION_KEY)
                .totalLapsRaced(57)
                .numOvertakes(23)
                .weatherCondition("Sunny")
                .avgRacePace(95.5)
                .safetyCarPeriods(1)
                .generatedAt(LocalDateTime.now())
                .build();

        when(queryService.getSessionSummary(SESSION_KEY)).thenReturn(summary);

        mockMvc.perform(get("/api/analytics/session/{sessionKey}/summary", SESSION_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionKey", is(SESSION_KEY)))
                .andExpect(jsonPath("$.totalLapsRaced", is(57)))
                .andExpect(jsonPath("$.numOvertakes", is(23)))
                .andExpect(jsonPath("$.weatherCondition", is("Sunny")))
                .andExpect(jsonPath("$.safetyCarPeriods", is(1)));
    }

    @Test
    @DisplayName("GET /health - returns UP")
    void healthCheck_returnsUp() throws Exception {
        mockMvc.perform(get("/api/analytics/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("analytics-service UP"));
    }
}
