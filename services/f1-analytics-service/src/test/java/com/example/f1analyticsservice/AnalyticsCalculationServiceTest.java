package com.example.f1analyticsservice;

import com.example.f1analyticsservice.dto.LapDataEvent;
import com.example.f1analyticsservice.models.DriverStats;
import com.example.f1analyticsservice.repository.DriverStatsRepository;
import com.example.f1analyticsservice.service.AnalyticsCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsCalculationService Tests")
class AnalyticsCalculationServiceTest {

    @Mock
    private DriverStatsRepository driverStatsRepository;

    @InjectMocks
    private AnalyticsCalculationService calculationService;

    @BeforeEach
    void setUp() {
        // Setup is handled by MockitoExtension
    }

    @Test
    @DisplayName("processLapData - creates new stats for new driver")
    void processLapData_newDriver_createsStats() {
        LapDataEvent lap = new LapDataEvent();
        lap.setSessionKey(9158);
        lap.setDriverNumber(1);
        lap.setLapNumber(1);
        lap.setLapDuration(95.123);
        lap.setSpeed(280.5);

        when(driverStatsRepository.findBySessionKeyAndDriverNumber(9158, 1))
                .thenReturn(Optional.empty());
        when(driverStatsRepository.save(any(DriverStats.class))).thenAnswer(inv -> inv.getArgument(0));

        calculationService.processLapData(lap);

        ArgumentCaptor<DriverStats> captor = ArgumentCaptor.forClass(DriverStats.class);
        verify(driverStatsRepository).save(captor.capture());

        DriverStats saved = captor.getValue();
        assertThat(saved.getSessionKey()).isEqualTo(9158);
        assertThat(saved.getDriverNumber()).isEqualTo(1);
        assertThat(saved.getTotalLaps()).isEqualTo(1);
        assertThat(saved.getFastestLapTime()).isEqualTo(95.123);
        assertThat(saved.getAvgLapTime()).isEqualTo(95.123);
        assertThat(saved.getMaxSpeed()).isEqualTo(280.5);
    }

    @Test
    @DisplayName("processLapData - updates existing stats")
    void processLapData_existingDriver_updatesStats() {
        LapDataEvent lap = new LapDataEvent();
        lap.setSessionKey(9158);
        lap.setDriverNumber(1);
        lap.setLapNumber(2);
        lap.setLapDuration(93.456);
        lap.setSpeed(290.0);

        DriverStats existing = DriverStats.builder()
                .id(1L)
                .sessionKey(9158)
                .driverNumber(1)
                .totalLaps(1)
                .fastestLapTime(95.123)
                .avgLapTime(95.123)
                .maxSpeed(280.5)
                .build();

        when(driverStatsRepository.findBySessionKeyAndDriverNumber(9158, 1))
                .thenReturn(Optional.of(existing));
        when(driverStatsRepository.save(any(DriverStats.class))).thenAnswer(inv -> inv.getArgument(0));

        calculationService.processLapData(lap);

        assertThat(existing.getTotalLaps()).isEqualTo(2);
        assertThat(existing.getFastestLapTime()).isEqualTo(93.456);
        assertThat(existing.getAvgLapTime()).isEqualTo((95.123 + 93.456) / 2);
        assertThat(existing.getMaxSpeed()).isEqualTo(290.0);
    }

    @Test
    @DisplayName("processLapData - slower lap does not update fastest")
    void processLapData_slowerLap_fastestUnchanged() {
        LapDataEvent lap = new LapDataEvent();
        lap.setSessionKey(9158);
        lap.setDriverNumber(1);
        lap.setLapNumber(2);
        lap.setLapDuration(96.0);
        lap.setSpeed(275.0);

        DriverStats existing = DriverStats.builder()
                .id(1L)
                .sessionKey(9158)
                .driverNumber(1)
                .totalLaps(1)
                .fastestLapTime(95.123)
                .avgLapTime(95.123)
                .maxSpeed(280.5)
                .build();

        when(driverStatsRepository.findBySessionKeyAndDriverNumber(9158, 1))
                .thenReturn(Optional.of(existing));
        when(driverStatsRepository.save(any(DriverStats.class))).thenAnswer(inv -> inv.getArgument(0));

        calculationService.processLapData(lap);

        assertThat(existing.getFastestLapTime()).isEqualTo(95.123);
        assertThat(existing.getMaxSpeed()).isEqualTo(280.5);
    }

    @Test
    @DisplayName("processLapData - null lap duration handled gracefully")
    void processLapData_nullDuration_handled() {
        LapDataEvent lap = new LapDataEvent();
        lap.setSessionKey(9158);
        lap.setDriverNumber(1);
        lap.setLapNumber(1);
        lap.setLapDuration(null);
        lap.setSpeed(280.0);

        when(driverStatsRepository.findBySessionKeyAndDriverNumber(9158, 1))
                .thenReturn(Optional.empty());
        when(driverStatsRepository.save(any(DriverStats.class))).thenAnswer(inv -> inv.getArgument(0));

        calculationService.processLapData(lap);

        ArgumentCaptor<DriverStats> captor = ArgumentCaptor.forClass(DriverStats.class);
        verify(driverStatsRepository).save(captor.capture());

        DriverStats saved = captor.getValue();
        assertThat(saved.getTotalLaps()).isEqualTo(1);
        assertThat(saved.getAvgLapTime()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("processLapData - null speed handled gracefully")
    void processLapData_nullSpeed_handled() {
        LapDataEvent lap = new LapDataEvent();
        lap.setSessionKey(9158);
        lap.setDriverNumber(1);
        lap.setLapNumber(1);
        lap.setLapDuration(95.0);
        lap.setSpeed(null);

        when(driverStatsRepository.findBySessionKeyAndDriverNumber(9158, 1))
                .thenReturn(Optional.empty());
        when(driverStatsRepository.save(any(DriverStats.class))).thenAnswer(inv -> inv.getArgument(0));

        calculationService.processLapData(lap);

        ArgumentCaptor<DriverStats> captor = ArgumentCaptor.forClass(DriverStats.class);
        verify(driverStatsRepository).save(captor.capture());

        DriverStats saved = captor.getValue();
        assertThat(saved.getMaxSpeed()).isEqualTo(0.0);
    }
}
