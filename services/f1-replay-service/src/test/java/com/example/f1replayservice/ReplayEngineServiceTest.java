package com.example.f1replayservice;

import com.example.f1replayservice.dto.ReplayControlDTO;
import com.example.f1replayservice.dto.ReplayFrameDTO;
import com.example.f1replayservice.dto.ReplaySessionDTO;
import com.example.f1replayservice.model.ReplayEvent;
import com.example.f1replayservice.model.ReplaySession;
import com.example.f1replayservice.repository.ReplayEventRepository;
import com.example.f1replayservice.repository.ReplaySessionRepository;
import com.example.f1replayservice.service.ReplayEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReplayEngineService Tests")
class ReplayEngineServiceTest {

    @Mock
    private ReplaySessionRepository sessionRepository;
    @Mock
    private ReplayEventRepository eventRepository;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private KafkaTemplate<String, ReplayFrameDTO> kafkaTemplate;
    @Mock
    private WebClient.Builder webClientBuilder;

    @InjectMocks
    private ReplayEngineService replayEngineService;

    private ReplaySession testSession;
    private ReplayEvent testEvent;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        testSession = ReplaySession.builder()
                .id(1L)
                .originalSessionKey(9158)
                .replayName("Test Replay")
                .status(ReplaySession.Status.CREATED)
                .currentTimeOffset(0L)
                .speedMultiplier(1.0)
                .build();

        testEvent = ReplayEvent.builder()
                .id(1L)
                .replaySession(testSession)
                .eventType("LAP_TIME")
                .eventTimeOffset(60000L)
                .driverNumber(1)
                .payload("{\"lapTime\": 95.123}")
                .build();
    }

    @Test
    @DisplayName("createReplay - creates new replay session")
    void createReplay_newSession_creates() {
        when(sessionRepository.save(any(ReplaySession.class))).thenAnswer(inv -> {
            ReplaySession s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        ReplaySessionDTO result = replayEngineService.createReplay(9158, "Test Replay");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getOriginalSessionKey()).isEqualTo(9158);
        assertThat(result.getReplayName()).isEqualTo("Test Replay");
        assertThat(result.getStatus()).isEqualTo("CREATED");
    }

    @Test
    @DisplayName("controlReplay - PLAY starts playback")
    void controlReplay_play_startsPlayback() {
        ReplaySession session = ReplaySession.builder()
                .id(1L)
                .status(ReplaySession.Status.READY)
                .currentTimeOffset(0L)
                .speedMultiplier(1.0)
                .build();

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(ReplaySession.class))).thenAnswer(inv -> inv.getArgument(0));

        ReplayControlDTO control = new ReplayControlDTO();
        control.setReplaySessionId(1L);
        control.setAction("PLAY");

        replayEngineService.controlReplay(control);

        assertThat(session.getStatus()).isEqualTo(ReplaySession.Status.PLAYING);
        assertThat(session.getStartedAt()).isNotNull();
    }

    @Test
    @DisplayName("controlReplay - PAUSE pauses playback")
    void controlReplay_pause_pausesPlayback() {
        ReplaySession session = ReplaySession.builder()
                .id(1L)
                .status(ReplaySession.Status.PLAYING)
                .currentTimeOffset(60000L)
                .speedMultiplier(1.0)
                .build();

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(ReplaySession.class))).thenAnswer(inv -> inv.getArgument(0));

        ReplayControlDTO control = new ReplayControlDTO();
        control.setReplaySessionId(1L);
        control.setAction("PAUSE");

        replayEngineService.controlReplay(control);

        assertThat(session.getStatus()).isEqualTo(ReplaySession.Status.PAUSED);
    }

    @Test
    @DisplayName("controlReplay - STOP stops playback")
    void controlReplay_stop_stopsPlayback() {
        ReplaySession session = ReplaySession.builder()
                .id(1L)
                .status(ReplaySession.Status.PLAYING)
                .build();

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(ReplaySession.class))).thenAnswer(inv -> inv.getArgument(0));

        ReplayControlDTO control = new ReplayControlDTO();
        control.setReplaySessionId(1L);
        control.setAction("STOP");

        replayEngineService.controlReplay(control);

        assertThat(session.getStatus()).isEqualTo(ReplaySession.Status.COMPLETED);
        assertThat(session.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("controlReplay - SEEK changes offset")
    void controlReplay_seek_changesOffset() {
        ReplaySession session = ReplaySession.builder()
                .id(1L)
                .status(ReplaySession.Status.PLAYING)
                .currentTimeOffset(0L)
                .build();

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(ReplaySession.class))).thenAnswer(inv -> inv.getArgument(0));

        ReplayControlDTO control = new ReplayControlDTO();
        control.setReplaySessionId(1L);
        control.setAction("SEEK");
        control.setSeekOffset(300000L);

        replayEngineService.controlReplay(control);

        assertThat(session.getCurrentTimeOffset()).isEqualTo(300000L);
    }

    @Test
    @DisplayName("controlReplay - SPEED changes multiplier")
    void controlReplay_speed_changesMultiplier() {
        ReplaySession session = ReplaySession.builder()
                .id(1L)
                .status(ReplaySession.Status.PLAYING)
                .speedMultiplier(1.0)
                .build();

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(ReplaySession.class))).thenAnswer(inv -> inv.getArgument(0));

        ReplayControlDTO control = new ReplayControlDTO();
        control.setReplaySessionId(1L);
        control.setAction("SPEED");
        control.setSpeed(2.0);

        replayEngineService.controlReplay(control);

        assertThat(session.getSpeedMultiplier()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("controlReplay - unknown action throws exception")
    void controlReplay_unknownAction_throws() {
        ReplaySession session = ReplaySession.builder()
                .id(1L)
                .status(ReplaySession.Status.READY)
                .build();

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        ReplayControlDTO control = new ReplayControlDTO();
        control.setReplaySessionId(1L);
        control.setAction("INVALID");

        assertThatThrownBy(() -> replayEngineService.controlReplay(control))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown action");
    }

    @Test
    @DisplayName("getCurrentFrame - returns frame with events")
    void getCurrentFrame_returnsFrame() {
        when(eventRepository.findByReplaySessionIdAndEventTimeOffsetBetween(1L, 0L, 1000L))
                .thenReturn(List.of(testEvent));
        when(eventRepository.findByReplaySessionIdOrderByEventTimeOffsetAsc(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of(testEvent)));

        ReplayFrameDTO result = replayEngineService.getCurrentFrame(1L);

        assertThat(result.getReplaySessionId()).isEqualTo(1L);
        assertThat(result.getCurrentOffset()).isEqualTo(0L);
        assertThat(result.getEvents()).hasSize(1);
        assertThat(result.getEvents().get(0).getEventType()).isEqualTo("LAP_TIME");
    }

    @Test
    @DisplayName("getCurrentFrame - empty events returns empty frame")
    void getCurrentFrame_emptyEvents_returnsEmpty() {
        when(eventRepository.findByReplaySessionIdAndEventTimeOffsetBetween(1L, 0L, 1000L))
                .thenReturn(List.of());
        when(eventRepository.findByReplaySessionIdOrderByEventTimeOffsetAsc(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of()));

        ReplayFrameDTO result = replayEngineService.getCurrentFrame(1L);

        assertThat(result.getEvents()).isEmpty();
        assertThat(result.isLastFrame()).isTrue();
    }

    @Test
    @DisplayName("getCurrentFrame - reconstructs from Redis when not in memory")
    void getCurrentFrame_fromRedis_reconstructs() {
        when(valueOperations.get("replay:state:1")).thenReturn("60000:1.0");
        when(eventRepository.findByReplaySessionIdAndEventTimeOffsetBetween(1L, 60000L, 61000L))
                .thenReturn(List.of());
        when(eventRepository.findByReplaySessionIdOrderByEventTimeOffsetAsc(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of()));

        ReplayFrameDTO result = replayEngineService.getCurrentFrame(1L);

        assertThat(result).isNotNull();
    }
}
