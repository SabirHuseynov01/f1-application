package com.example.f1telemetryservice.service;

import com.example.f1telemetryservice.dto.PositionDTO;
import com.example.f1telemetryservice.dto.TelemetryDTO;
import com.example.f1telemetryservice.dto.TelemetrySnapshotDTO;
import com.example.f1telemetryservice.model.CarTelemetry;
import com.example.f1telemetryservice.model.PositionData;
import com.example.f1telemetryservice.repository.CarTelemetryRepository;
import com.example.f1telemetryservice.repository.PositionDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TelemetryQueryService {

    private final CarTelemetryRepository carTelemetryRepository;
    private final PositionDataRepository positionDataRepository;

    @Cacheable(value = "latestTelemetry", key = "#sessionKey")
    public TelemetrySnapshotDTO getLatestSnapshot(Integer sessionKey) {
        List<CarTelemetry> latest = carTelemetryRepository.findLatestBySession(sessionKey);

        List<TelemetrySnapshotDTO.DriverTelemetryDTO> drivers = latest.stream()
                .map(t -> {
                    PositionData pos = positionDataRepository
                            .findTopBySessionKeyAndDriverNumberOrderByTimestampDesc(
                                    sessionKey, t.getDriverNumber())
                            .orElse(null);

                    return TelemetrySnapshotDTO.DriverTelemetryDTO.builder()
                            .driverNumber(t.getDriverNumber())
                            .speed(t.getSpeed())
                            .gear(t.getGear())
                            .throttle(t.getThrottle())
                            .drs(t.getDrs())
                            .position(pos != null ? PositionDTO.builder()
                                    .x(pos.getX()).y(pos.getY()).z(pos.getZ())
                                    .lapDistance(pos.getLapDistance())
                                    .build() : null)
                            .build();
                })
                .collect(Collectors.toList());

        return TelemetrySnapshotDTO.builder()
                .sessionKey(sessionKey)
                .drivers(drivers)
                .build();
    }

    public List<TelemetryDTO> getDriverTelemetry(Integer sessionKey, Integer driverNumber) {
        return carTelemetryRepository.findBySessionKeyAndDriverNumber(sessionKey, driverNumber)
                .stream()
                .map(t -> TelemetryDTO.builder()
                        .sessionKey(t.getSessionKey())
                        .driverNumber(t.getDriverNumber())
                        .speed(t.getSpeed())
                        .rpm(t.getRpm())
                        .gear(t.getGear())
                        .throttle(t.getThrottle())
                        .brake(t.getBrake())
                        .drs(t.getDrs())
                        .timestamp(t.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }
}