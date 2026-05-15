package com.example.f1replayservice.service;

import com.example.f1replayservice.dto.ReplayControlDTO;
import com.example.f1replayservice.dto.ReplayEventDTO;
import com.example.f1replayservice.dto.ReplayFrameDTO;
import com.example.f1replayservice.dto.ReplaySessionDTO;
import com.example.f1replayservice.model.ReplayEvent;
import com.example.f1replayservice.model.ReplaySession;
import com.example.f1replayservice.repository.ReplayEventRepository;
import com.example.f1replayservice.repository.ReplaySessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReplayEngineService {

    private final ReplaySessionRepository sessionRepository;
    private final ReplayEventRepository eventRepository;
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, ReplayFrameDTO> kafkaTemplate;
    private final WebClient.Builder webClientBuilder;

    // In-memory state for active replays
    private final ConcurrentHashMap<Long, ReplayState> activeReplays = new ConcurrentHashMap<>();

    private static final String REPLAY_STATE_PREFIX = "replay:state:";
    private static final long FRAME_INTERVAL_MS = 1000; // 1 second frames
    private static final int EVENTS_PER_FRAME = 100;

    @Transactional
    public ReplaySessionDTO createReplay(Integer sessionKey, String replayName) {
        // Fetch session data from session-service
        validateSessionExists(sessionKey);

        ReplaySession session = ReplaySession.builder()
                .originalSessionKey(sessionKey)
                .replayName(replayName)
                .status(ReplaySession.Status.CREATED)
                .speedMultiplier(1.0)
                .build();

        ReplaySession saved = sessionRepository.save(session);
        log.info("Created replay session: {} for original session: {}", saved.getId(), sessionKey);

        // Async data loading
        loadSessionDataAsync(saved.getId(), sessionKey);

        return mapToDTO(saved);
    }

    @Transactional
    public void controlReplay(ReplayControlDTO control) {
        ReplaySession session = sessionRepository.findById(control.getReplaySessionId())
                .orElseThrow(() -> new RuntimeException("Replay not found"));

        switch (control.getAction().toUpperCase()) {
            case "PLAY" -> startPlayback(session);
            case "PAUSE" -> pausePlayback(session);
            case "STOP" -> stopPlayback(session);
            case "SEEK" -> seekTo(session, control.getSeekOffset());
            case "SPEED" -> setSpeed(session, control.getSpeed());
            default -> throw new IllegalArgumentException("Unknown action: " + control.getAction());
        }
    }

    public ReplayFrameDTO getCurrentFrame(Long replaySessionId) {
        ReplayState state = activeReplays.get(replaySessionId);
        if (state == null) {
            // Try to reconstruct from Redis
            return reconstructFrameFromRedis(replaySessionId);
        }

        long currentOffset = state.currentOffset;
        List<ReplayEvent> events = eventRepository
                .findByReplaySessionIdAndEventTimeOffsetBetween(
                        replaySessionId,
                        currentOffset,
                        currentOffset + FRAME_INTERVAL_MS);

        boolean isLast = events.size() < EVENTS_PER_FRAME ||
                eventRepository.findByReplaySessionIdOrderByEventTimeOffsetAsc(
                                replaySessionId, PageRequest.of(0, 1))
                        .stream().findFirst()
                        .map(e -> e.getEventTimeOffset() <= currentOffset)
                        .orElse(true);

        return ReplayFrameDTO.builder()
                .replaySessionId(replaySessionId)
                .currentOffset(currentOffset)
                .events(events.stream().map(this::mapToDTO).collect(Collectors.toList()))
                .isLastFrame(isLast)
                .build();
    }

    @Scheduled(fixedRate = 1000)
    public void processActiveReplays() {
        activeReplays.forEach((id, state) -> {
            if (state.status == ReplaySession.Status.PLAYING) {
                state.currentOffset += (long) (FRAME_INTERVAL_MS * state.speedMultiplier);
                redisTemplate.opsForValue().set(
                        REPLAY_STATE_PREFIX + id,
                        state.currentOffset + ":" + state.speedMultiplier);

                ReplayFrameDTO frame = getCurrentFrame(id);
                kafkaTemplate.send("f1.replay.events", id.toString(), frame);
            }
        });
    }

    private void startPlayback(ReplaySession session) {
        session.setStatus(ReplaySession.Status.PLAYING);
        session.setStartedAt(java.time.LocalDateTime.now());
        sessionRepository.save(session);

        ReplayState state = activeReplays.computeIfAbsent(session.getId(), k -> new ReplayState());
        state.status = ReplaySession.Status.PLAYING;
        state.currentOffset = session.getCurrentTimeOffset();
        state.speedMultiplier = session.getSpeedMultiplier();

        log.info("Started replay: {}", session.getId());
    }

    private void pausePlayback(ReplaySession session) {
        session.setStatus(ReplaySession.Status.PAUSED);
        session.setCurrentTimeOffset(activeReplays.get(session.getId()).currentOffset);
        sessionRepository.save(session);

        Optional.ofNullable(activeReplays.get(session.getId()))
                .ifPresent(s -> s.status = ReplaySession.Status.PAUSED);

        log.info("Paused replay: {}", session.getId());
    }

    private void stopPlayback(ReplaySession session) {
        session.setStatus(ReplaySession.Status.COMPLETED);
        session.setCompletedAt(java.time.LocalDateTime.now());
        sessionRepository.save(session);

        activeReplays.remove(session.getId());
        redisTemplate.delete(REPLAY_STATE_PREFIX + session.getId());

        log.info("Stopped replay: {}", session.getId());
    }

    private void seekTo(ReplaySession session, Long offset) {
        Optional.ofNullable(activeReplays.get(session.getId()))
                .ifPresent(s -> s.currentOffset = offset);
        session.setCurrentTimeOffset(offset);
        sessionRepository.save(session);
    }

    private void setSpeed(ReplaySession session, Double speed) {
        Optional.ofNullable(activeReplays.get(session.getId()))
                .ifPresent(s -> s.speedMultiplier = speed);
        session.setSpeedMultiplier(speed);
        sessionRepository.save(session);
    }

    private void validateSessionExists(Integer sessionKey) {
        // Call session-service
        webClientBuilder.build()
                .get()
                .uri("http://session-service:8082/api/v1/sessions/key/" + sessionKey)
                .retrieve()
                .toBodilessEntity()
                .blockOptional()
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionKey));
    }

    private void loadSessionDataAsync(Long replayId, Integer sessionKey) {
        // Fetch laps, stints, telemetry from other services and create replay events
        // This would be implemented with async WebClient calls
        log.info("Loading data for replay: {} from session: {}", replayId, sessionKey);

        // Mark as READY when done
        ReplaySession session = sessionRepository.findById(replayId).orElseThrow();
        session.setStatus(ReplaySession.Status.READY);
        sessionRepository.save(session);
    }

    private ReplayFrameDTO reconstructFrameFromRedis(Long replaySessionId) {
        String state = redisTemplate.opsForValue().get(REPLAY_STATE_PREFIX + replaySessionId);
        if (state == null) {
            return ReplayFrameDTO.builder()
                    .replaySessionId(replaySessionId)
                    .events(List.of())
                    .isLastFrame(true)
                    .build();
        }
        String[] parts = state.split(":");
        long offset = Long.parseLong(parts[0]);
        return getCurrentFrame(replaySessionId);
    }

    private ReplaySessionDTO mapToDTO(ReplaySession session) {
        return ReplaySessionDTO.builder()
                .id(session.getId())
                .originalSessionKey(session.getOriginalSessionKey())
                .replayName(session.getReplayName())
                .status(session.getStatus().name())
                .currentTimeOffset(session.getCurrentTimeOffset())
                .speedMultiplier(session.getSpeedMultiplier())
                .createdAt(session.getCreatedAt())
                .startedAt(session.getStartedAt())
                .build();
    }

    private ReplayEventDTO mapToDTO(ReplayEvent event) {
        return ReplayEventDTO.builder()
                .eventType(event.getEventType())
                .eventTimeOffset(event.getEventTimeOffset())
                .driverNumber(event.getDriverNumber())
                .payload(event.getPayload())
                .build();
    }

    private static class ReplayState {
        volatile ReplaySession.Status status;
        volatile long currentOffset;
        volatile double speedMultiplier = 1.0;
    }
}
