package com.example.f1timingservice.service;


import com.example.f1timingservice.client.OpenF1TimingClient;
import com.example.f1timingservice.dto.openF1.OpenF1LapDTO;
import com.example.f1timingservice.dto.openF1.OpenF1StintDTO;
import com.example.f1timingservice.model.LapTime;
import com.example.f1timingservice.model.Stint;
import com.example.f1timingservice.repository.LapTimeRepository;
import com.example.f1timingservice.repository.StintRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimingSyncService {

    private final OpenF1TimingClient openF1TimingClient;
    private final LapTimeRepository lapTimeRepository;
    private final StintRepository stintRepository;
    private final ObjectMapper objectMapper;

    @Async
    @Transactional
    public CompletableFuture<Void> syncLapsBySessionAsync(Integer sessionKey) {
        syncLapsBySession(sessionKey);
        return CompletableFuture.completedFuture(null);
    }

    @Transactional
    public void syncLapsBySession(Integer sessionKey) {
        log.info("Lap sync started - session: {}", sessionKey);

        List<OpenF1LapDTO> laps = openF1TimingClient.fetchLapsBySession(sessionKey);
        log.info("Session {} for {} lap found ", sessionKey, laps.size());

        int saved = 0;
        int skipped = 0;

        for (OpenF1LapDTO dto : laps) {
            boolean exists = lapTimeRepository
                    .findBySessionKeyAndDriverNumberAndLapNumber(
                            dto.getSessionKey(),
                            dto.getDriverNumber(),
                            dto.getLapNumber())
                    .isPresent();

            if (!exists) {
                lapTimeRepository.save(mapToLapTime(dto));
                saved++;
            } else {
                skipped++;
            }
        }

        log.info("Lap sync completed — session: {}, saved: {}, skipped: {}", sessionKey, saved, skipped);
    }

    @Async
    @Transactional
    public CompletableFuture<Void> syncStintsBySessionAsync(Integer sessionKey) {
        syncStintsBySession(sessionKey);
        return CompletableFuture.completedFuture(null);
    }

    @Transactional
    public void syncStintsBySession(Integer sessionKey) {
        log.info("Stint sync started - session: {}", sessionKey);

        List<OpenF1StintDTO> stints = openF1TimingClient.fetchStintsBySession(sessionKey);
        log.info("Session {} for {} stint found ", sessionKey, stints.size());

        int saved = 0;
        int skipped = 0;

        for (OpenF1StintDTO dto : stints) {
            boolean exists = stintRepository
                    .findBySessionKeyAndDriverNumberAndStintNumber(
                            dto.getSessionKey(),
                            dto.getDriverNumber(),
                            dto.getStintNumber())
                    .isPresent();

            if (!exists) {
                stintRepository.save(mapToStint(dto));
                saved++;
            } else {
                skipped++;
            }
        }

        log.info("Stint sync completed — session: {}, saved: {}, skipped: {}", sessionKey, saved, skipped);
    }

    @Async
    @Transactional
    public CompletableFuture<Void> syncSessionAsync(Integer sessionKey) {
        log.info("Syncing all laps for session: {}", sessionKey);
        syncLapsBySession(sessionKey);
        syncStintsBySession(sessionKey);
        log.info("All laps for session: {} synced", sessionKey);
        return CompletableFuture.completedFuture(null);
    }

    private Stint mapToStint(OpenF1StintDTO dto) {
        return Stint.builder()
                .sessionKey(dto.getSessionKey())
                .driverNumber(dto.getDriverNumber())
                .stintNumber(dto.getStintNumber())
                .lapStart(dto.getLapStart())
                .lapEnd(dto.getLapEnd())
                .compound(dto.getCompound())
                .tyreAgeAtStart(dto.getTyreAgeAtStart())
                .build();
    }

    private LapTime mapToLapTime(OpenF1LapDTO dto) {
        return LapTime.builder()
                .sessionKey(dto.getSessionKey())
                .driverNumber(dto.getDriverNumber())
                .lapNumber(dto.getLapNumber())
                .lapDuration(dto.getLapDuration())
                .sector1Duration(dto.getDurationSector1())
                .sector2Duration(dto.getDurationSector2())
                .sector3Duration(dto.getDurationSector3())
                .isPitOutLap(dto.getIsPitOutLap())
                .segmentsSector1(toJson(dto.getSegmentsSector1()))
                .segmentsSector2(toJson(dto.getSegmentsSector2()))
                .segmentsSector3(toJson(dto.getSegmentsSector3()))
                .build();
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("JSON serialize hatası: {}", e.getMessage());
            return null;
        }
    }


}
