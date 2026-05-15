package com.example.f1sessionservice;

import com.example.f1sessionservice.exception.SessionNotFoundException;
import com.example.f1sessionservice.model.*;
import com.example.f1sessionservice.repository.CircuitRepository;
import com.example.f1sessionservice.repository.SeasonRepository;
import com.example.f1sessionservice.repository.SessionDriverRepository;
import com.example.f1sessionservice.repository.SessionRepository;
import com.example.f1sessionservice.service.SessionQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionQueryServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SessionDriverRepository sessionDriverRepository;

    @Mock
    private SeasonRepository seasonRepository;

    @Mock
    private CircuitRepository circuitRepository;

    @InjectMocks
    private SessionQueryService sessionQueryService;

    private Session testSession;
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
                .name("Silverstone Circuit")
                .country("United Kingdom")
                .city("Silverstone")
                .build();

        testSession = Session.builder()
                .id(1L)
                .sessionKey(9523)
                .season(testSeason)
                .circuit(testCircuit)
                .sessionType(SessionType.RACE)
                .dateStart(LocalDateTime.of(2024, 7, 7, 15, 0))
                .dateEnd(LocalDateTime.of(2024, 7, 7, 17, 0))
                .build();
    }

    @Test
    void getSessionsByYear_ShouldReturnListOfSessions() {
        when(sessionRepository.findBySeasonYear(2024))
                .thenReturn(List.of(testSession));

        List<Session> result = sessionQueryService.getSessionsByYear(2024);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(9523, result.get(0).getSessionKey());
        verify(sessionRepository, times(1)).findBySeasonYear(2024);
    }

    @Test
    void getSessionByKey_WhenExists_ShouldReturnSession() {
        when(sessionRepository.findBySessionKey(9523))
                .thenReturn(Optional.of(testSession));

        Session result = sessionQueryService.getSessionByKey(9523);

        assertNotNull(result);
        assertEquals(9523, result.getSessionKey());
        assertEquals("Silverstone Circuit", result.getCircuit().getName());
    }

    @Test
    void getSessionByKey_WhenNotExists_ShouldThrowException() {
        when(sessionRepository.findBySessionKey(9999))
                .thenReturn(Optional.empty());

        assertThrows(SessionNotFoundException.class, () ->
                sessionQueryService.getSessionByKey(9999));
    }

    @Test
    void getDriversBySession_ShouldReturnDriverList() {
        SessionDrivers driver = SessionDrivers.builder()
                .id(1L)
                .session(testSession)
                .driverNumber(1)
                .driverCode("VER")
                .fullName("Max Verstappen")
                .teamName("Red Bull Racing")
                .build();

        when(sessionDriverRepository.findBySessionId(1L))
                .thenReturn(List.of(driver));

        List<SessionDrivers> result = sessionQueryService.getDriversBySession(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("VER", result.get(0).getDriverCode());
    }

    @Test
    void getAllSeasons_ShouldReturnAllSeasons() {
        when(seasonRepository.findAll())
                .thenReturn(List.of(testSeason));

        List<Season> result = sessionQueryService.getAllSeasons();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2024, result.get(0).getYear());
    }

    @Test
    void getAllCircuits_ShouldReturnAllCircuits() {
        when(circuitRepository.findAll())
                .thenReturn(List.of(testCircuit));

        List<Circuit> result = sessionQueryService.getAllCircuits();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Silverstone Circuit", result.get(0).getName());
    }
}

