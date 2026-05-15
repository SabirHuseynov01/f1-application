package com.example.f1sessionservice;


import com.example.f1sessionservice.client.OpenF1Client;
import com.example.f1sessionservice.dto.openF1.OpenF1SessionDTO;
import com.example.f1sessionservice.model.Circuit;
import com.example.f1sessionservice.model.Season;
import com.example.f1sessionservice.model.Session;
import com.example.f1sessionservice.repository.CircuitRepository;
import com.example.f1sessionservice.repository.SeasonRepository;
import com.example.f1sessionservice.repository.SessionDriverRepository;
import com.example.f1sessionservice.repository.SessionRepository;
import com.example.f1sessionservice.service.SessionSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionSyncServiceTest {

    @Mock
    private OpenF1Client openF1Client;

    @Mock
    private SeasonRepository seasonRepository;

    @Mock
    private CircuitRepository circuitRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SessionDriverRepository sessionDriverRepository;

    @InjectMocks
    private SessionSyncService sessionSyncService;

    private OpenF1SessionDTO testSessionDTO;
    private Season testSeason;
    private Circuit testCircuit;

    @BeforeEach
    void setUp() {
        testSeason = Season.builder()
                .id(1L)
                .year(2024)
                .championshipName("F1 2024")
                .build();

        testCircuit = Circuit.builder()
                .id(1L)
                .circuitKey(14)
                .name("Silverstone")
                .country("UK")
                .city("Silverstone")
                .build();

        testSessionDTO = new OpenF1SessionDTO();
        testSessionDTO.setSessionKey(9523);
        testSessionDTO.setSessionName("Race");
        testSessionDTO.setSessionType("Race");
        testSessionDTO.setYear(2024);
        testSessionDTO.setCircuitKey(14);
        testSessionDTO.setCircuitShortName("Silverstone");
        testSessionDTO.setCountryName("United Kingdom");
        testSessionDTO.setLocation("Silverstone");
        testSessionDTO.setDateStart(OffsetDateTime.now());
        testSessionDTO.setDateEnd(OffsetDateTime.now().plusHours(2));
    }

    @Test
    void syncByYear_WhenNewSession_ShouldCreateSession() {
        when(seasonRepository.findByYear(2024))
                .thenReturn(Optional.of(testSeason));
        when(openF1Client.fetchSessionsByYear(2024))
                .thenReturn(List.of(testSessionDTO));
        when(sessionRepository.findBySessionKey(9523))
                .thenReturn(Optional.empty());
        when(circuitRepository.findByCircuitKey(14))
                .thenReturn(Optional.of(testCircuit));
        when(sessionRepository.save(any(Session.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> sessionSyncService.syncByYear(2024));

        verify(sessionRepository, times(1)).save(any(Session.class));
    }

    @Test
    void syncByYear_WhenSessionExists_ShouldSkip() {
        Session existingSession = Session.builder()
                .sessionKey(9523)
                .build();

        when(seasonRepository.findByYear(2024))
                .thenReturn(Optional.of(testSeason));
        when(openF1Client.fetchSessionsByYear(2024))
                .thenReturn(List.of(testSessionDTO));
        when(sessionRepository.findBySessionKey(9523))
                .thenReturn(Optional.of(existingSession));

        sessionSyncService.syncByYear(2024);

        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    void syncByYear_WhenCircuitNotExists_ShouldCreateCircuit() {
        when(seasonRepository.findByYear(2024))
                .thenReturn(Optional.of(testSeason));
        when(openF1Client.fetchSessionsByYear(2024))
                .thenReturn(List.of(testSessionDTO));
        when(sessionRepository.findBySessionKey(9523))
                .thenReturn(Optional.empty());
        when(circuitRepository.findByCircuitKey(14))
                .thenReturn(Optional.empty());
        when(circuitRepository.save(any(Circuit.class)))
                .thenReturn(testCircuit);
        when(sessionRepository.save(any(Session.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> sessionSyncService.syncByYear(2024));

        verify(circuitRepository, times(1)).save(any(Circuit.class));
    }
}

