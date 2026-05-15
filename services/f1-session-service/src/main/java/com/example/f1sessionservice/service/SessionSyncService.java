package com.example.f1sessionservice.service;

import com.example.f1sessionservice.client.OpenF1Client;
import com.example.f1sessionservice.dto.openF1.OpenF1DriverDTO;
import com.example.f1sessionservice.dto.openF1.OpenF1SessionDTO;
import com.example.f1sessionservice.dto.response.SyncResultDTO;
import com.example.f1sessionservice.exception.SessionNotFoundException;
import com.example.f1sessionservice.model.*;
import com.example.f1sessionservice.repository.CircuitRepository;
import com.example.f1sessionservice.repository.SeasonRepository;
import com.example.f1sessionservice.repository.SessionDriverRepository;
import com.example.f1sessionservice.repository.SessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionSyncService {

    private final OpenF1Client openF1Client;
    private final SeasonRepository seasonRepository;
    private final CircuitRepository circuitRepository;
    private final SessionRepository sessionRepository;
    private final SessionDriverRepository sessionDriverRepository;

    @Scheduled(cron = "0 0 3 * * *")
    public void syncCurrentSeason() {
        int currentYear = LocalDate.now().getYear();
        log.info("OpenF1 sync started - year: {}", currentYear);
        try {
            syncByYear(currentYear);
        } catch (Exception e) {
            log.error("Auto sync unsuccessful: {}", e.getMessage());
        }
    }

    @Transactional
    public SyncResultDTO syncByYear(Integer year) {
        SyncResultDTO.SyncResultDTOBuilder resultBuilder = SyncResultDTO.builder()
                .year(year)
                .status("STARTED");

        Season season = seasonRepository.findByYear(year)
                .orElseGet(() -> {
                    try {
                        return seasonRepository.save(
                                Season.builder()
                                        .year(year)
                                        .championshipName("Formula 1 World Championship " + year)
                                        .build()
                        );
                    } catch (DataIntegrityViolationException e) {
                        return seasonRepository.findByYear(year)
                                .orElseThrow(() -> new RuntimeException("Season not created: " + year));
                    }
                });

        // OpenF1'den session'ları çek
        List<OpenF1SessionDTO> openF1Sessions = openF1Client.fetchSessionsByYear(year);
        log.info("Found {} sessions for year {}", year, openF1Sessions.size());

        int createdCount = 0;
        int skippedCount = 0;
        int testDaySkipped = 0;
        int driversCreated = 0;

        for (OpenF1SessionDTO dto : openF1Sessions) {
            log.info("RAW OpenF1 session: key={}, name='{}', type='{}', circuit={}",
                    dto.getSessionKey(),
                    dto.getSessionName(),
                    dto.getSessionType(),
                    dto.getCircuitShortName());

            if (isTestDay(dto.getSessionName())) {
                log.info("Skipping test day: session_key={}, name='{}'", dto.getSessionKey(), dto.getSessionName());
                testDaySkipped++;
                continue;
            }

            if (sessionRepository.findBySessionKey(dto.getSessionKey()).isPresent()) {
                skippedCount++;
                continue;
            }

            Circuit circuit = circuitRepository.findByCircuitKey(dto.getCircuitKey())
                    .orElseGet(() -> {
                        try {
                            return circuitRepository.save(
                                    Circuit.builder()
                                            .circuitKey(dto.getCircuitKey())
                                            .name(dto.getCircuitShortName())
                                            .country(dto.getCountryName())
                                            .city(dto.getLocation())
                                            .build()
                            );
                        } catch (DataIntegrityViolationException e) {
                            return circuitRepository.findByCircuitKey(dto.getCircuitKey())
                                    .orElseThrow(() -> new RuntimeException("Circuit not created: " + dto.getCircuitKey()));
                        }
                    });

            SessionType sessionType = determineSessionType(dto);

            Session session = sessionRepository.save(
                    Session.builder()
                            .sessionKey(dto.getSessionKey())
                            .season(season)
                            .circuit(circuit)
                            .sessionType(sessionType)
                            .dateStart(dto.getDateStart() != null ? dto.getDateStart().toLocalDateTime() : null)
                            .dateEnd(dto.getDateEnd() != null ? dto.getDateEnd().toLocalDateTime() : null)
                            .build()
            );

            try {
                int newDrivers = syncDriversForSessionInternal(session);
                driversCreated += newDrivers;
                log.info("Drivers synced for new session: {} ({} new)", session.getSessionKey(), newDrivers);
            } catch (Exception e) {
                log.warn("Driver sync failed for session {}: {}", session.getSessionKey(), e.getMessage());
            }

            createdCount++;
        }

        SyncResultDTO result = resultBuilder
                .sessionsCreated(createdCount)
                .sessionsSkipped(skippedCount)
                .driversCreated(driversCreated)
                .status("COMPLETED")
                .build();

        log.info("Sync completed — year: {}, created: {}, skipped: {}, testDaysSkipped: {}, drivers: {}",
                year, createdCount, skippedCount, testDaySkipped, driversCreated);

        return result;
    }

    @Transactional
    public SyncResultDTO syncDriversForSessionKey(Integer sessionKey) {
        Session session = sessionRepository.findBySessionKey(sessionKey)
                .orElseThrow(() -> new SessionNotFoundException(sessionKey));

        int createdCount = syncDriversForSessionInternal(session);

        return SyncResultDTO.builder()
                .year(session.getSeason().getYear())
                .sessionsCreated(0)
                .sessionsSkipped(0)
                .driversCreated(createdCount)
                .status("DRIVER_SYNC_COMPLETED")
                .build();
    }

    private int syncDriversForSessionInternal(Session session) {
        List<OpenF1DriverDTO> drivers = openF1Client.fetchDriversBySession(session.getSessionKey());
        log.info("Found {} drivers for session {}", drivers.size(), session.getSessionKey());

        int createdCount = 0;

        for (OpenF1DriverDTO dto : drivers) {
            boolean exists = sessionDriverRepository
                    .findBySessionIdAndDriverNumber(session.getId(), dto.getDriverNumber())
                    .isPresent();

            if (!exists) {
                try {
                    sessionDriverRepository.save(
                            SessionDrivers.builder()
                                    .session(session)
                                    .driverNumber(dto.getDriverNumber())
                                    .driverCode(dto.getNameAcronym())
                                    .fullName(dto.getFullName())
                                    .teamName(dto.getTeamName())
                                    .teamColour(dto.getTeamColour())
                                    .build()
                    );
                    createdCount++;
                } catch (DataIntegrityViolationException e) {
                    log.warn("Driver already exists: session={}, driver={}",
                            session.getSessionKey(), dto.getDriverNumber());
                }
            }
        }

        log.info("Driver sync completed - session: {}, created: {}", session.getSessionKey(), createdCount);
        return createdCount;
    }

    private boolean isTestDay(String sessionName) {
        if (sessionName == null) {
            return false;
        }
        String normalized = sessionName.trim().toLowerCase();
        return normalized.equals("day 1") ||
                normalized.equals("day 2") ||
                normalized.equals("day 3");
    }

    private SessionType determineSessionType(OpenF1SessionDTO dto) {
        String name = dto.getSessionName();
        String type = dto.getSessionType();

        if (name != null && !name.trim().isEmpty()) {
            String normalized = name.toLowerCase().trim();

            switch (normalized) {
                case "practice 1", "free practice 1", "fp1" -> {
                    return SessionType.FP1;
                }
                case "practice 2", "free practice 2", "fp2" -> {
                    return SessionType.FP2;
                }
                case "practice 3", "free practice 3", "fp3" -> {
                    return SessionType.FP3;
                }
                case "sprint qualifying", "sprint_qualifying", "sprint shootout" -> {
                    return SessionType.SPRINT_QUALIFYING;
                }
                case "sprint" -> {
                    return SessionType.SPRINT;
                }
                case "qualifying" -> {
                    return SessionType.QUALIFYING;
                }
                case "race" -> {
                    return SessionType.RACE;
                }
                default -> {
                    log.warn("Unknown session name: '{}', trying session_type", name);
                }
            }
        }

        if (type != null && !type.trim().isEmpty()) {
            String normalized = type.toLowerCase().trim();

            switch (normalized) {
                case "practice" -> {
                    return SessionType.FP1;
                }
                case "qualifying" -> {
                    return SessionType.QUALIFYING;
                }
                case "race" -> {
                    return SessionType.RACE;
                }
                default -> {
                    log.warn("Unknown session type: '{}', assigned as RACE", type);
                    return SessionType.RACE;
                }
            }
        }

        log.warn("Both sessionName and sessionType null/empty, assigned as RACE");
        return SessionType.RACE;
    }
}
