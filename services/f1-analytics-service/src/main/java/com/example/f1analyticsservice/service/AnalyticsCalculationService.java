package com.example.f1analyticsservice.service;

import com.example.f1analyticsservice.dto.LapDataEvent;
import com.example.f1analyticsservice.models.DriverStats;
import com.example.f1analyticsservice.repository.DriverStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsCalculationService {

    private final DriverStatsRepository driverStatsRepository;


    @Transactional
    public void processLapData(LapDataEvent lap){
        DriverStats stats = driverStatsRepository
                .findBySessionKeyAndDriverNumber(lap.getSessionKey(), lap.getDriverNumber())
                .orElse(DriverStats.builder()
                        .sessionKey(lap.getSessionKey())
                        .driverNumber(lap.getDriverNumber())
                        .totalLaps(0)
                        .fastestLapTime(Double.MAX_VALUE)
                        .avgLapTime(0.0)
                        .maxSpeed(0.0)
                        .build());

        stats.setTotalLaps(stats.getTotalLaps() + 1);

        if (lap.getLapDuration() != null && lap.getLapDuration() < stats.getFastestLapTime()) {
            stats.setFastestLapTime(lap.getLapDuration());
        }

        // Update rolling average
        double currentAvg = stats.getAvgLapTime();
        int n = stats.getTotalLaps();
        stats.setAvgLapTime(((currentAvg * (n - 1)) + (lap.getLapDuration() != null ? lap.getLapDuration() : 0)) / n);

        if (lap.getSpeed() != null && lap.getSpeed() > stats.getMaxSpeed()) {
            stats.setMaxSpeed(lap.getSpeed());
        }

        driverStatsRepository.save(stats);
        log.info("Updated stats for session={}, driver={}", lap.getSessionKey(), lap.getDriverNumber());
    }
}
