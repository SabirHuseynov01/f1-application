package com.example.f1analyticsservice;

import com.example.f1analyticsservice.dto.DriverStatsDTO;
import com.example.f1analyticsservice.dto.SessionSummaryDTO;
import com.example.f1analyticsservice.models.DriverStats;
import com.example.f1analyticsservice.models.SessionSummary;
import com.example.f1analyticsservice.repository.DriverStatsRepository;
import com.example.f1analyticsservice.repository.SessionSummaryRepository;
import com.example.f1analyticsservice.service.AnalyticsQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsQueryService Tests")
class AnalyticsQueryServiceTest {

    @Mock
    private DriverStatsRepository driverStatsRepository;

    @Mock
    private SessionSummaryRepository sessionSummaryRepository;

    @InjectMocks
    private AnalyticsQueryService queryService;

    private static final Integer SESSION_KEY = 9158;

    private DriverStats stats1, stats2;
    private SessionSummary summary;

    @BeforeEach
    void setUp() {
        stats1 = DriverStats.builder()
                .id(1L)
                .sessionKey(SESSION_KEY)
                .driverNumber(1)
                .totalLaps(57)
                .fastestLapTime(92.456)
                .avgLapTime(95.123)
                .totalPitStops(2)
                .maxSpeed(325.5)
                .avgSpeed(210.3)
                .calculatedAt(LocalDateTime.now())
                .build();

        stats2 = DriverStats.builder()
                .id(2L)
                .sessionKey(SESSION_KEY)
                .driverNumber(44)
                .totalLaps(57)
                .fastestLapTime(91.892)
                .avgLapTime(94.567)
                .totalPitStops(2)
                .maxSpeed(328.1)
                .avgSpeed(212.5)
                .calculatedAt(LocalDateTime.now())
                .build();

        summary = SessionSummary.builder()
                .id(1L)
                .sessionKey(SESSION_KEY)
                .totalLapsRaced(57)
                .numOvertakes(23)
                .weatherCondition("Sunny")
                .avgRacePace(95.5)
                .safetyCarPeriods(1)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getSessionDriverStats - returns all driver stats for session")
    void getSessionDriverStats_returnsAllDrivers() {
        when(driverStatsRepository.findBySessionKey(SESSION_KEY))
                .thenReturn(List.of(stats1, stats2));

        List<DriverStatsDTO> result = queryService.getSessionDriverStats(SESSION_KEY);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDriverNumber()).isEqualTo(1);
        assertThat(result.get(1).getDriverNumber()).isEqualTo(44);
    }

    @Test
    @DisplayName("getSessionDriverStats - empty session returns empty list")
    void getSessionDriverStats_empty_returnsEmpty() {
        when(driverStatsRepository.findBySessionKey(SESSION_KEY)).thenReturn(List.of());

        List<DriverStatsDTO> result = queryService.getSessionDriverStats(SESSION_KEY);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getDriverStats - returns specific driver stats")
    void getDriverStats_returnsSpecificDriver() {
        when(driverStatsRepository.findBySessionKeyAndDriverNumber(SESSION_KEY, 1))
                .thenReturn(Optional.of(stats1));

        DriverStatsDTO result = queryService.getDriverStats(SESSION_KEY, 1);

        assertThat(result).isNotNull();
        assertThat(result.getDriverNumber()).isEqualTo(1);
        assertThat(result.getFastestLapTime()).isEqualTo(92.456);
    }

    @Test
    @DisplayName("getDriverStats - non-existing driver returns null")
    void getDriverStats_nonExisting_returnsNull() {
        when(driverStatsRepository.findBySessionKeyAndDriverNumber(SESSION_KEY, 999))
                .thenReturn(Optional.empty());

        DriverStatsDTO result = queryService.getDriverStats(SESSION_KEY, 999);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getSessionSummary - returns session summary")
    void getSessionSummary_returnsSummary() {
        when(sessionSummaryRepository.findBySessionKey(SESSION_KEY))
                .thenReturn(Optional.of(summary));

        SessionSummaryDTO result = queryService.getSessionSummary(SESSION_KEY);

        assertThat(result).isNotNull();
        assertThat(result.getSessionKey()).isEqualTo(SESSION_KEY);
        assertThat(result.getTotalLapsRaced()).isEqualTo(57);
        assertThat(result.getNumOvertakes()).isEqualTo(23);
        assertThat(result.getWeatherCondition()).isEqualTo("Sunny");
    }

    @Test
    @DisplayName("getSessionSummary - non-existing session returns null")
    void getSessionSummary_nonExisting_returnsNull() {
        when(sessionSummaryRepository.findBySessionKey(SESSION_KEY))
                .thenReturn(Optional.empty());

        SessionSummaryDTO result = queryService.getSessionSummary(SESSION_KEY);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("mapToDTO - fastestLapTime Double.MAX_VALUE maps to null")
    void mapToDTO_maxValueFastestLap_mapsToNull() {
        DriverStats statsWithMax = DriverStats.builder()
                .sessionKey(SESSION_KEY)
                .driverNumber(1)
                .totalLaps(0)
                .fastestLapTime(Double.MAX_VALUE)
                .build();

        when(driverStatsRepository.findBySessionKeyAndDriverNumber(SESSION_KEY, 1))
                .thenReturn(Optional.of(statsWithMax));

        DriverStatsDTO result = queryService.getDriverStats(SESSION_KEY, 1);

        assertThat(result.getFastestLapTime()).isNull();
    }
}