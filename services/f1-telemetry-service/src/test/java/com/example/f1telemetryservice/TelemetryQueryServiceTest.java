package com.example.f1telemetryservice;

import com.example.f1telemetryservice.dto.TelemetryDTO;
import com.example.f1telemetryservice.dto.TelemetrySnapshotDTO;
import com.example.f1telemetryservice.model.CarTelemetry;
import com.example.f1telemetryservice.model.PositionData;
import com.example.f1telemetryservice.repository.CarTelemetryRepository;
import com.example.f1telemetryservice.repository.PositionDataRepository;
import com.example.f1telemetryservice.service.TelemetryQueryService;
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
@DisplayName("TelemetryQueryService Tests")
class TelemetryQueryServiceTest {

    @Mock
    private CarTelemetryRepository carTelemetryRepository;

    @Mock
    private PositionDataRepository positionDataRepository;

    @InjectMocks
    private TelemetryQueryService queryService;

    private static final Integer SESSION_KEY = 9158;

    private CarTelemetry telemetry1, telemetry2;
    private PositionData position1, position2;

    @BeforeEach
    void setUp() {
        telemetry1 = CarTelemetry.builder()
                .id(1L)
                .sessionKey(SESSION_KEY)
                .driverNumber(1)
                .speed(285.5)
                .rpm(10500)
                .gear(7)
                .throttle(100.0)
                .brake(0.0)
                .drs(1.0)
                .timestamp(LocalDateTime.now())
                .build();

        telemetry2 = CarTelemetry.builder()
                .id(2L)
                .sessionKey(SESSION_KEY)
                .driverNumber(44)
                .speed(290.2)
                .rpm(10800)
                .gear(7)
                .throttle(98.5)
                .brake(0.0)
                .drs(1.0)
                .timestamp(LocalDateTime.now())
                .build();

        position1 = PositionData.builder()
                .id(1L)
                .sessionKey(SESSION_KEY)
                .driverNumber(1)
                .x(100.0)
                .y(200.0)
                .z(0.0)
                .lapDistance(1500.0)
                .timestamp(LocalDateTime.now())
                .build();

        position2 = PositionData.builder()
                .id(2L)
                .sessionKey(SESSION_KEY)
                .driverNumber(44)
                .x(105.0)
                .y(205.0)
                .z(0.0)
                .lapDistance(1520.0)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getLatestSnapshot - returns snapshot with latest data for all drivers")
    void getLatestSnapshot_returnsSnapshotWithAllDrivers() {
        when(carTelemetryRepository.findLatestBySession(SESSION_KEY))
                .thenReturn(List.of(telemetry1, telemetry2));
        when(positionDataRepository.findTopBySessionKeyAndDriverNumberOrderByTimestampDesc(SESSION_KEY, 1))
                .thenReturn(Optional.of(position1));
        when(positionDataRepository.findTopBySessionKeyAndDriverNumberOrderByTimestampDesc(SESSION_KEY, 44))
                .thenReturn(Optional.of(position2));

        TelemetrySnapshotDTO result = queryService.getLatestSnapshot(SESSION_KEY);

        assertThat(result.getSessionKey()).isEqualTo(SESSION_KEY);
        assertThat(result.getDrivers()).hasSize(2);
        assertThat(result.getDrivers().get(0).getDriverNumber()).isEqualTo(1);
        assertThat(result.getDrivers().get(0).getSpeed()).isEqualTo(285.5);
        assertThat(result.getDrivers().get(0).getPosition().getLapDistance()).isEqualTo(1500.0);
        assertThat(result.getDrivers().get(1).getDriverNumber()).isEqualTo(44);
        assertThat(result.getDrivers().get(1).getPosition().getX()).isEqualTo(105.0);
    }

    @Test
    @DisplayName("getLatestSnapshot - handles missing position data gracefully")
    void getLatestSnapshot_missingPositionData_returnsNullPosition() {
        when(carTelemetryRepository.findLatestBySession(SESSION_KEY))
                .thenReturn(List.of(telemetry1));
        when(positionDataRepository.findTopBySessionKeyAndDriverNumberOrderByTimestampDesc(SESSION_KEY, 1))
                .thenReturn(Optional.empty());

        TelemetrySnapshotDTO result = queryService.getLatestSnapshot(SESSION_KEY);

        assertThat(result.getDrivers()).hasSize(1);
        assertThat(result.getDrivers().get(0).getPosition()).isNull();
    }

    @Test
    @DisplayName("getLatestSnapshot - empty session returns empty drivers list")
    void getLatestSnapshot_emptySession_returnsEmpty() {
        when(carTelemetryRepository.findLatestBySession(SESSION_KEY)).thenReturn(List.of());

        TelemetrySnapshotDTO result = queryService.getLatestSnapshot(SESSION_KEY);

        assertThat(result.getDrivers()).isEmpty();
    }

    @Test
    @DisplayName("getDriverTelemetry - returns telemetry list for specific driver")
    void getDriverTelemetry_returnsDriverTelemetry() {
        when(carTelemetryRepository.findBySessionKeyAndDriverNumber(SESSION_KEY, 1))
                .thenReturn(List.of(telemetry1));

        List<TelemetryDTO> result = queryService.getDriverTelemetry(SESSION_KEY, 1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDriverNumber()).isEqualTo(1);
        assertThat(result.get(0).getSpeed()).isEqualTo(285.5);
        assertThat(result.get(0).getRpm()).isEqualTo(10500);
    }

    @Test
    @DisplayName("getDriverTelemetry - non-existing driver returns empty list")
    void getDriverTelemetry_nonExisting_returnsEmpty() {
        when(carTelemetryRepository.findBySessionKeyAndDriverNumber(SESSION_KEY, 999))
                .thenReturn(List.of());

        List<TelemetryDTO> result = queryService.getDriverTelemetry(SESSION_KEY, 999);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getLatestSnapshot - multiple drivers sorted correctly")
    void getLatestSnapshot_multipleDrivers_sorted() {
        when(carTelemetryRepository.findLatestBySession(SESSION_KEY))
                .thenReturn(List.of(telemetry2, telemetry1));
        when(positionDataRepository.findTopBySessionKeyAndDriverNumberOrderByTimestampDesc(any(), any()))
                .thenReturn(Optional.empty());

        TelemetrySnapshotDTO result = queryService.getLatestSnapshot(SESSION_KEY);

        assertThat(result.getDrivers()).hasSize(2);
    }
}
