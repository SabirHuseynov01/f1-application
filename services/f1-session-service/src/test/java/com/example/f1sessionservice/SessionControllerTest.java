package com.example.f1sessionservice;

import com.example.f1sessionservice.controller.SessionController;
import com.example.f1sessionservice.dto.response.DriverResponseDTO;
import com.example.f1sessionservice.dto.response.SessionResponseDTO;
import com.example.f1sessionservice.exception.SessionNotFoundException;
import com.example.f1sessionservice.mapper.SessionMapper;
import com.example.f1sessionservice.model.*;
import com.example.f1sessionservice.service.CircuitSyncService;
import com.example.f1sessionservice.service.SessionQueryService;
import com.example.f1sessionservice.service.SessionSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SessionController.class)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SessionQueryService queryService;

    @MockitoBean
    private SessionSyncService syncService;

    @MockitoBean
    private SessionMapper mapper;

    @MockitoBean
    private CircuitSyncService circuitSyncService;

    private Session testSession;
    private SessionResponseDTO testSessionDTO;

    @BeforeEach
    void setUp() {
        Season season = Season.builder().id(1L).year(2024).build();
        Circuit circuit = Circuit.builder().id(1L).name("Silverstone").country("UK").city("Silverstone").build();

        testSession = Session.builder()
                .id(1L)
                .sessionKey(9523)
                .season(season)
                .circuit(circuit)
                .sessionType(SessionType.RACE)
                .dateStart(LocalDateTime.of(2024, 7, 7, 15, 0))
                .dateEnd(LocalDateTime.of(2024, 7, 7, 17, 0))
                .build();

        testSessionDTO = SessionResponseDTO.builder()
                .id(1L)
                .sessionKey(9523)
                .seasonYear(2024)
                .circuitName("Silverstone")
                .country("UK")
                .city("Silverstone")
                .sessionType(SessionType.RACE)
                .dateStart(LocalDateTime.of(2024, 7, 7, 15, 0))
                .dateEnd(LocalDateTime.of(2024, 7, 7, 17, 0))
                .build();
    }

    @Test
    void getByYear_ShouldReturnSessions() throws Exception {
        when(queryService.getSessionsByYear(2024))
                .thenReturn(List.of(testSession));
        when(mapper.toSessionDTO(testSession))
                .thenReturn(testSessionDTO);

        mockMvc.perform(get("/api/v1/sessions/year/2024")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sessionKey", is(9523)))
                .andExpect(jsonPath("$[0].circuitName", is("Silverstone")));
    }

    @Test
    void getByKey_WhenExists_ShouldReturnSession() throws Exception {
        when(queryService.getSessionByKey(9523))
                .thenReturn(testSession);
        when(mapper.toSessionDTO(testSession))
                .thenReturn(testSessionDTO);

        mockMvc.perform(get("/api/v1/sessions/key/9523")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionKey", is(9523)))
                .andExpect(jsonPath("$.sessionType", is("RACE")));
    }

    @Test
    void getByKey_WhenNotExists_ShouldReturn404() throws Exception {
        when(queryService.getSessionByKey(9999))
                .thenThrow(new SessionNotFoundException(9999));

        mockMvc.perform(get("/api/v1/sessions/key/9999")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void syncYear_ShouldReturnSuccess() throws Exception {
        doNothing().when(syncService).syncByYear(2024);

        mockMvc.perform(post("/api/v1/sessions/sync/2024")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sync completed")));
    }

    @Test
    void getDrivers_ShouldReturnDriverList() throws Exception {
        SessionDrivers driver = SessionDrivers.builder()
                .id(1L)
                .driverNumber(1)
                .driverCode("VER")
                .fullName("Max Verstappen")
                .teamName("Red Bull Racing")
                .build();

        DriverResponseDTO driverDTO = DriverResponseDTO.builder()
                .id(1L)
                .driverNumber(1)
                .driverCode("VER")
                .fullName("Max Verstappen")
                .teamName("Red Bull Racing")
                .build();

        when(queryService.getDriversBySession(1L))
                .thenReturn(List.of(driver));
        when(mapper.toDriverDTO(driver))
                .thenReturn(driverDTO);

        mockMvc.perform(get("/api/v1/sessions/1/drivers")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].driverCode", is("VER")))
                .andExpect(jsonPath("$[0].fullName", is("Max Verstappen")));
    }

    @Test
    void getByYear_WithInvalidYear_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/sessions/year/1900")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isBadRequest());
    }
}
