package com.example.f1timingservice.service;


import com.example.f1timingservice.exception.SessionNotFoundException;
import com.example.f1timingservice.model.LapTime;
import com.example.f1timingservice.model.Stint;
import com.example.f1timingservice.repository.LapTimeRepository;
import com.example.f1timingservice.repository.StintRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TimingQueryService {

    private final LapTimeRepository lapTimeRepository;
    private final StintRepository stintRepository;

    @Cacheable(value = "lapsBySession", key = "#sessionKey")
    public List<LapTime> getLapsBySession(Integer sessionKey) {
        validateSessionExists(sessionKey);
        return lapTimeRepository.findBySessionKey(sessionKey);
    }

    public Page<LapTime> getLapsBySessionPaginated(Integer sessionKey, int page, int size) {
        validateSessionExists(sessionKey);
        Pageable pageable = PageRequest.of(page, size);
        return lapTimeRepository.findBySessionKey(sessionKey, pageable);
    }

    @Cacheable(value = "driverLaps", key = "#sessionKey + '_' + #driverNumber")
    public List<LapTime> getDriverLaps(Integer sessionKey, Integer driverNumber) {
        return lapTimeRepository.findDriverLapsOrdered(sessionKey, driverNumber);
    }

    @Cacheable(value = "fastestLaps", key = "#sessionKey")
    public List<LapTime> getFastestLaps(Integer sessionKey) {
        return lapTimeRepository.findFastestLapsBySession(sessionKey);
    }

    @Cacheable(value = "stintsBySession", key = "#sessionKey")
    public List<Stint> getStintsBySession(Integer sessionKey) {
        return stintRepository.findBySessionKey(sessionKey);
    }

    @Cacheable(value = "driverStints", key = "#sessionKey + '-' + #driverNumber")
    public List<Stint> getDriverStints(Integer sessionKey, Integer driverNumber) {
        return stintRepository.findBySessionKeyAndDriverNumber(sessionKey, driverNumber);
    }

    private void validateSessionExists(Integer sessionKey) {
        long lapCount = lapTimeRepository.countBySessionKey(sessionKey);
        long stintCount = stintRepository.countBySessionKey(sessionKey);

        if (lapCount == 0 && stintCount == 0) {
            log.warn("Session bulunamadı: {}", sessionKey);
            throw new SessionNotFoundException(sessionKey);
        }
    }
}
