package com.example.f1analyticsservice.service;

import com.example.f1analyticsservice.dto.DriverStatsDTO;
import com.example.f1analyticsservice.dto.SessionSummaryDTO;
import com.example.f1analyticsservice.models.DriverStats;
import com.example.f1analyticsservice.models.SessionSummary;
import com.example.f1analyticsservice.repository.DriverStatsRepository;
import com.example.f1analyticsservice.repository.SessionSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsQueryService {

    private final SessionSummaryRepository sessionSummaryRepository;
    private final DriverStatsRepository driverStatsRepository;

    public List<DriverStatsDTO> getSessionDriverStats(Integer sessionKey) {
        return driverStatsRepository.findBySessionKey(sessionKey).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public DriverStatsDTO getDriverStats(Integer sessionKey, Integer driverNumber) {
        return driverStatsRepository.findBySessionKeyAndDriverNumber(sessionKey, driverNumber)
                .map(this::mapToDTO)
                .orElse(null);
    }

    public SessionSummaryDTO getSessionSummary(Integer sessionKey) {
        return sessionSummaryRepository.findBySessionKey(sessionKey)
                .map(this::mapToDTO)
                .orElse(null);
    }

    private DriverStatsDTO mapToDTO(DriverStats stats) {
        return DriverStatsDTO.builder()
                .sessionKey(stats.getSessionKey())
                .driverNumber(stats.getDriverNumber())
                .totalLaps(stats.getTotalLaps())
                .fastestLapTime(stats.getFastestLapTime() == Double.MAX_VALUE ? null : stats.getFastestLapTime())
                .avgLapTime(stats.getAvgLapTime())
                .totalPitStops(stats.getTotalPitStops())
                .maxSpeed(stats.getMaxSpeed())
                .avgSpeed(stats.getAvgSpeed())
                .calculatedAt(stats.getCalculatedAt())
                .build();
    }

    private SessionSummaryDTO mapToDTO(SessionSummary summary) {
        return SessionSummaryDTO.builder()
                .sessionKey(summary.getSessionKey())
                .totalLapsRaced(summary.getTotalLapsRaced())
                .numOvertakes(summary.getNumOvertakes())
                .weatherCondition(summary.getWeatherCondition())
                .avgRacePace(summary.getAvgRacePace())
                .safetyCarPeriods(summary.getSafetyCarPeriods())
                .generatedAt(summary.getGeneratedAt())
                .build();
    }
}
