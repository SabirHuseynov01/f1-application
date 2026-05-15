package com.example.f1replayservice;

import com.example.f1replayservice.controller.ReplayController;
import com.example.f1replayservice.dto.ReplayControlDTO;
import com.example.f1replayservice.dto.ReplayEventDTO;
import com.example.f1replayservice.dto.ReplayFrameDTO;
import com.example.f1replayservice.dto.ReplaySessionDTO;
import com.example.f1replayservice.service.ReplayEngineService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReplayController Tests")
class ReplayControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ReplayEngineService replayEngineService;

    @InjectMocks
    private ReplayController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /create/{sessionKey} - creates replay session")
    void createReplay_validSession_returnsReplay() throws Exception {
        ReplaySessionDTO replay = ReplaySessionDTO.builder()
                .id(1L)
                .originalSessionKey(9158)
                .replayName("Bahrain GP Replay")
                .status("CREATED")
                .currentTimeOffset(0L)
                .speedMultiplier(1.0)
                .createdAt(LocalDateTime.now())
                .build();

        when(replayEngineService.createReplay(9158, "Bahrain GP Replay")).thenReturn(replay);

        mockMvc.perform(post("/api/replay/create/{sessionKey}", 9158)
                        .param("name", "Bahrain GP Replay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.originalSessionKey", is(9158)))
                .andExpect(jsonPath("$.replayName", is("Bahrain GP Replay")))
                .andExpect(jsonPath("$.status", is("CREATED")))
                .andExpect(jsonPath("$.speedMultiplier", is(1.0)));
    }

    @Test
    @DisplayName("POST /control - controls replay playback")
    void controlReplay_playAction_returnsOk() throws Exception {
        ReplayControlDTO control = new ReplayControlDTO();
        control.setReplaySessionId(1L);
        control.setAction("PLAY");

        doNothing().when(replayEngineService).controlReplay(any(ReplayControlDTO.class));

        mockMvc.perform(post("/api/replay/control")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(control)))
                .andExpect(status().isOk());

        verify(replayEngineService).controlReplay(any(ReplayControlDTO.class));
    }

    @Test
    @DisplayName("POST /control - pause action")
    void controlReplay_pauseAction_returnsOk() throws Exception {
        ReplayControlDTO control = new ReplayControlDTO();
        control.setReplaySessionId(1L);
        control.setAction("PAUSE");

        doNothing().when(replayEngineService).controlReplay(any(ReplayControlDTO.class));

        mockMvc.perform(post("/api/replay/control")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(control)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /control - seek action")
    void controlReplay_seekAction_returnsOk() throws Exception {
        ReplayControlDTO control = new ReplayControlDTO();
        control.setReplaySessionId(1L);
        control.setAction("SEEK");
        control.setSeekOffset(300000L);

        doNothing().when(replayEngineService).controlReplay(any(ReplayControlDTO.class));

        mockMvc.perform(post("/api/replay/control")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(control)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /control - speed action")
    void controlReplay_speedAction_returnsOk() throws Exception {
        ReplayControlDTO control = new ReplayControlDTO();
        control.setReplaySessionId(1L);
        control.setAction("SPEED");
        control.setSpeed(2.0);

        doNothing().when(replayEngineService).controlReplay(any(ReplayControlDTO.class));

        mockMvc.perform(post("/api/replay/control")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(control)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /{replaySessionId}/frame - returns current frame")
    void getFrame_returnsFrame() throws Exception {
        ReplayFrameDTO frame = ReplayFrameDTO.builder()
                .replaySessionId(1L)
                .currentOffset(60000L)
                .events(List.of(
                        ReplayEventDTO.builder()
                                .eventType("LAP_TIME")
                                .eventTimeOffset(60000L)
                                .driverNumber(1)
                                .payload("{\"lapTime\": 95.123}")
                                .build()
                ))
                .isLastFrame(false)
                .build();

        when(replayEngineService.getCurrentFrame(1L)).thenReturn(frame);

        mockMvc.perform(get("/api/replay/{replaySessionId}/frame", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.replaySessionId", is(1)))
                .andExpect(jsonPath("$.currentOffset", is(60000)))
                .andExpect(jsonPath("$.events", hasSize(1)))
                .andExpect(jsonPath("$.events[0].eventType", is("LAP_TIME")))
                .andExpect(jsonPath("$.isLastFrame", is(false)));
    }

    @Test
    @DisplayName("GET /{replaySessionId}/frame - last frame returns isLastFrame true")
    void getFrame_lastFrame_returnsLast() throws Exception {
        ReplayFrameDTO frame = ReplayFrameDTO.builder()
                .replaySessionId(1L)
                .currentOffset(5400000L)
                .events(List.of())
                .isLastFrame(true)
                .build();

        when(replayEngineService.getCurrentFrame(1L)).thenReturn(frame);

        mockMvc.perform(get("/api/replay/{replaySessionId}/frame", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isLastFrame", is(true)));
    }

    @Test
    @DisplayName("GET /health - returns UP")
    void healthCheck_returnsUp() throws Exception {
        mockMvc.perform(get("/api/replay/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("replay-service UP"));
    }
}
