package com.example.f1timingservice;

import com.example.f1timingservice.controller.TimingController;
import com.example.f1timingservice.dto.response.LapTimeResponseDTO;
import com.example.f1timingservice.dto.response.StintResponseDTO;
import com.example.f1timingservice.exception.SessionNotFoundException;
import com.example.f1timingservice.exception.TimingControllerAdvice;
import com.example.f1timingservice.mapper.TimingMapper;
import com.example.f1timingservice.model.LapTime;
import com.example.f1timingservice.model.Stint;
import com.example.f1timingservice.service.TimingQueryService;
import com.example.f1timingservice.service.TimingSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimingController Tests")
class TimingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TimingQueryService queryService;

    @Mock
    private TimingSyncService syncService;

    @Mock
    private TimingMapper mapper;

    @InjectMocks
    private TimingController controller;

    private static final Integer SESSION_KEY = 9158;
    private static final Integer DRIVER_NUMBER = 44;

    private LapTime lap1, lap2;
    private Stint stint1;
    private LapTimeResponseDTO lapDTO1, lapDTO2;
    private StintResponseDTO stintDTO1;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new TimingControllerAdvice())
                .build();

        lap1 = LapTime.builder()
                .id(1L).sessionKey(SESSION_KEY).driverNumber(44)
                .lapNumber(1).lapDuration(95.123)
                .sector1Duration(28.1).sector2Duration(34.5).sector3Duration(32.4)
                .isPitOutLap(false).build();

        lap2 = LapTime.builder()
                .id(2L).sessionKey(SESSION_KEY).driverNumber(44)
                .lapNumber(2).lapDuration(92.456).isPitOutLap(false).build();

        stint1 = Stint.builder()
                .id(1L).sessionKey(SESSION_KEY).driverNumber(1)
                .stintNumber(1).lapStart(1).lapEnd(22)
                .compound("MEDIUM").tyreAgeAtStart(0).build();

        lapDTO1 = LapTimeResponseDTO.builder()
                .id(1L).sessionKey(SESSION_KEY).driverNumber(44)
                .lapNumber(1).lapDuration(95.123)
                .sector1Duration(28.1).sector2Duration(34.5).sector3Duration(32.4)
                .isPitOutLap(false).build();

        lapDTO2 = LapTimeResponseDTO.builder()
                .id(2L).sessionKey(SESSION_KEY).driverNumber(44)
                .lapNumber(2).lapDuration(92.456).isPitOutLap(false).build();

        stintDTO1 = StintResponseDTO.builder()
                .id(1L).sessionKey(SESSION_KEY).driverNumber(1)
                .stintNumber(1).lapStart(1).lapEnd(22)
                .compound("MEDIUM").tyreAgeAtStart(0).build();
    }

    // ─── GET /api/timing/laps/{sessionKey} ───────────────────────────────────

    @Test
    @DisplayName("GET /laps/{sessionKey} - 200 OK və lap siyahısı")
    void getLaps_existingSession_returns200WithLaps() throws Exception {
        when(queryService.getLapsBySession(SESSION_KEY)).thenReturn(List.of(lap1, lap2));
        when(mapper.toLapDTO(lap1)).thenReturn(lapDTO1);
        when(mapper.toLapDTO(lap2)).thenReturn(lapDTO2);

        mockMvc.perform(get("/api/timing/laps/{sessionKey}", SESSION_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].sessionKey").value(SESSION_KEY))
                .andExpect(jsonPath("$[0].driverNumber").value(44))
                .andExpect(jsonPath("$[0].lapNumber").value(1))
                .andExpect(jsonPath("$[0].lapDuration").value(95.123))
                .andExpect(jsonPath("$[1].lapNumber").value(2));
    }

    @Test
    @DisplayName("GET /laps/{sessionKey} - mövcud olmayan session - 404 Not Found")
    void getLaps_nonExistingSession_returns404() throws Exception {
        when(queryService.getLapsBySession(9999))
                .thenThrow(new SessionNotFoundException(9999));

        mockMvc.perform(get("/api/timing/laps/{sessionKey}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("GET /laps/{sessionKey} - boş siyahı - 200 OK boş array")
    void getLaps_emptySession_returns200WithEmptyArray() throws Exception {
        when(queryService.getLapsBySession(SESSION_KEY)).thenReturn(List.of());

        mockMvc.perform(get("/api/timing/laps/{sessionKey}", SESSION_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ─── GET /api/timing/laps/{sessionKey}/driver/{driverNumber} ─────────────

    @Test
    @DisplayName("GET /laps/{sessionKey}/driver/{driverNumber} - 200 OK sürücü lapları")
    void getDriverLaps_existingDriver_returns200() throws Exception {
        when(queryService.getDriverLaps(SESSION_KEY, DRIVER_NUMBER)).thenReturn(List.of(lap1, lap2));
        when(mapper.toLapDTO(lap1)).thenReturn(lapDTO1);
        when(mapper.toLapDTO(lap2)).thenReturn(lapDTO2);

        mockMvc.perform(get("/api/timing/laps/{sessionKey}/driver/{driverNumber}",
                        SESSION_KEY, DRIVER_NUMBER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].driverNumber").value(44));
    }

    @Test
    @DisplayName("GET /laps/{sessionKey}/driver/{driverNumber} - mövcud olmayan sürücü - boş array")
    void getDriverLaps_nonExistingDriver_returnsEmptyArray() throws Exception {
        when(queryService.getDriverLaps(SESSION_KEY, 999)).thenReturn(List.of());

        mockMvc.perform(get("/api/timing/laps/{sessionKey}/driver/{driverNumber}",
                        SESSION_KEY, 999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ─── GET /api/timing/laps/{sessionKey}/fastest ───────────────────────────

    @Test
    @DisplayName("GET /laps/{sessionKey}/fastest - 200 OK ən sürətli laplar")
    void getFastestLaps_existingSession_returns200() throws Exception {
        // lap2 daha sürətli (92.456)
        when(queryService.getFastestLaps(SESSION_KEY)).thenReturn(List.of(lap2, lap1));
        when(mapper.toLapDTO(lap2)).thenReturn(lapDTO2);
        when(mapper.toLapDTO(lap1)).thenReturn(lapDTO1);

        mockMvc.perform(get("/api/timing/laps/{sessionKey}/fastest", SESSION_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].lapDuration").value(92.456));
    }

    // ─── GET /api/timing/stints/{sessionKey} ─────────────────────────────────

    @Test
    @DisplayName("GET /stints/{sessionKey} - 200 OK stint siyahısı")
    void getStints_existingSession_returns200WithStints() throws Exception {
        when(queryService.getStintsBySession(SESSION_KEY)).thenReturn(List.of(stint1));
        when(mapper.toStintDTO(stint1)).thenReturn(stintDTO1);

        mockMvc.perform(get("/api/timing/stints/{sessionKey}", SESSION_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].compound").value("MEDIUM"))
                .andExpect(jsonPath("$[0].lapStart").value(1))
                .andExpect(jsonPath("$[0].lapEnd").value(22));
    }

    @Test
    @DisplayName("GET /stints/{sessionKey} - mövcud olmayan session - 404")
    void getStints_nonExistingSession_returns404() throws Exception {
        when(queryService.getStintsBySession(9999))
                .thenThrow(new SessionNotFoundException(9999));

        mockMvc.perform(get("/api/timing/stints/{sessionKey}", 9999))
                .andExpect(status().isNotFound());
    }

    // ─── GET /api/timing/stints/{sessionKey}/driver/{driverNumber} ───────────

    @Test
    @DisplayName("GET /stints/{sessionKey}/driver/{driverNumber} - 200 OK")
    void getDriverStints_existingDriver_returns200() throws Exception {
        when(queryService.getDriverStints(SESSION_KEY, 1)).thenReturn(List.of(stint1));
        when(mapper.toStintDTO(stint1)).thenReturn(stintDTO1);

        mockMvc.perform(get("/api/timing/stints/{sessionKey}/driver/{driverNumber}",
                        SESSION_KEY, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].driverNumber").value(1));
    }

    // ─── POST /api/timing/sync/laps/{sessionKey} ─────────────────────────────

    @Test
    @DisplayName("POST /sync/laps/{sessionKey} - 200 OK sync başladı mesajı")
    void syncLaps_validSession_returns200() throws Exception {
        doNothing().when(syncService).syncLapsBySession(SESSION_KEY);

        mockMvc.perform(post("/api/timing/sync/laps/{sessionKey}", SESSION_KEY))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(String.valueOf(SESSION_KEY))));

        verify(syncService).syncLapsBySession(SESSION_KEY);
    }

    // ─── POST /api/timing/sync/stints/{sessionKey} ───────────────────────────

    @Test
    @DisplayName("POST /sync/stints/{sessionKey} - 200 OK sync başladı mesajı")
    void syncStints_validSession_returns200() throws Exception {
        doNothing().when(syncService).syncStintsBySession(SESSION_KEY);

        mockMvc.perform(post("/api/timing/sync/stints/{sessionKey}", SESSION_KEY))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(String.valueOf(SESSION_KEY))));

        verify(syncService).syncStintsBySession(SESSION_KEY);
    }

    // ─── POST /api/timing/sync/session/{sessionKey} ──────────────────────────

    @Test
    @DisplayName("POST /sync/session/{sessionKey} - 202 Accepted async sync")
    void syncFullSession_validSession_returns202() throws Exception {
        when(syncService.syncSessionAsync(SESSION_KEY))
                .thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(post("/api/timing/sync/session/{sessionKey}", SESSION_KEY))
                .andExpect(status().isAccepted())
                .andExpect(content().string(containsString(String.valueOf(SESSION_KEY))));

        verify(syncService).syncSessionAsync(SESSION_KEY);
    }

    // ─── GET /api/timing/health ───────────────────────────────────────────────

    @Test
    @DisplayName("GET /health - 200 OK timing-service UP")
    void healthCheck_returns200() throws Exception {
        mockMvc.perform(get("/api/timing/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("timing-service UP"));
    }
}
