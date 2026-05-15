package com.example.f1timingservice;

import com.example.f1timingservice.exception.SessionNotFoundException;
import com.example.f1timingservice.model.LapTime;
import com.example.f1timingservice.model.Stint;
import com.example.f1timingservice.repository.LapTimeRepository;
import com.example.f1timingservice.repository.StintRepository;
import com.example.f1timingservice.service.TimingQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimingQueryService Tests")
class TimingQueryServiceTest {

    @Mock
    private LapTimeRepository lapTimeRepository;

    @Mock
    private StintRepository stintRepository;

    @InjectMocks
    private TimingQueryService queryService;

    private static final Integer SESSION_KEY = 9158;
    private static final Integer DRIVER_NUMBER = 44;

    private LapTime lap1, lap2;
    private Stint stint1, stint2;

    @BeforeEach
    void setUp() {
        lap1 = LapTime.builder()
                .id(1L)
                .sessionKey(SESSION_KEY)
                .driverNumber(44)
                .lapNumber(1)
                .lapDuration(95.123)
                .sector1Duration(28.1)
                .sector2Duration(34.5)
                .sector3Duration(32.4)
                .isPitOutLap(false)
                .build();

        lap2 = LapTime.builder()
                .id(2L)
                .sessionKey(SESSION_KEY)
                .driverNumber(44)
                .lapNumber(2)
                .lapDuration(92.456)
                .sector1Duration(27.3)
                .sector2Duration(33.8)
                .sector3Duration(31.3)
                .isPitOutLap(false)
                .build();

        stint1 = Stint.builder()
                .id(1L)
                .sessionKey(SESSION_KEY)
                .driverNumber(1)
                .stintNumber(1)
                .lapStart(1)
                .lapEnd(22)
                .compound("MEDIUM")
                .tyreAgeAtStart(0)
                .build();

        stint2 = Stint.builder()
                .id(2L)
                .sessionKey(SESSION_KEY)
                .driverNumber(1)
                .stintNumber(2)
                .lapStart(23)
                .lapEnd(50)
                .compound("HARD")
                .tyreAgeAtStart(3)
                .build();
    }



    @Test
    @DisplayName("getLapsBySession - mövcud session - lap siyahısı qayıdır")
    void getLapsBySession_existingSession_returnsLaps() {
        when(lapTimeRepository.countBySessionKey(SESSION_KEY)).thenReturn(2L);
        when(stintRepository.countBySessionKey(SESSION_KEY)).thenReturn(2L);
        when(lapTimeRepository.findBySessionKey(SESSION_KEY)).thenReturn(List.of(lap1, lap2));

        List<LapTime> result = queryService.getLapsBySession(SESSION_KEY);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSessionKey()).isEqualTo(SESSION_KEY);
        assertThat(result.get(0).getLapNumber()).isEqualTo(1);
        assertThat(result.get(1).getLapNumber()).isEqualTo(2);
        verify(lapTimeRepository).findBySessionKey(SESSION_KEY);
    }

    @Test
    @DisplayName("getLapsBySession - mövcud olmayan session - SessionNotFoundException atılır")
    void getLapsBySession_nonExistingSession_throwsSessionNotFoundException() {
        when(lapTimeRepository.countBySessionKey(9999)).thenReturn(0L);
        when(stintRepository.countBySessionKey(9999)).thenReturn(0L);

        assertThatThrownBy(() -> queryService.getLapsBySession(9999))
                .isInstanceOf(SessionNotFoundException.class)
                .hasMessageContaining("9999");

        verify(lapTimeRepository, never()).findBySessionKey(any());
    }

    @Test
    @DisplayName("getLapsBySession - yalnız lap varsa session mövcud sayılır")
    void getLapsBySession_onlyLapsExist_sessionValid() {
        when(lapTimeRepository.countBySessionKey(SESSION_KEY)).thenReturn(5L);
        when(stintRepository.countBySessionKey(SESSION_KEY)).thenReturn(0L);
        when(lapTimeRepository.findBySessionKey(SESSION_KEY)).thenReturn(List.of(lap1));

        List<LapTime> result = queryService.getLapsBySession(SESSION_KEY);

        assertThat(result).hasSize(1);
    }

    // ─── getLapsBySessionPaginated ────────────────────────────────────────────

    @Test
    @DisplayName("getLapsBySessionPaginated - düzgün page və size - Page qayıdır")
    void getLapsBySessionPaginated_validPageAndSize_returnsPage() {
        when(lapTimeRepository.countBySessionKey(SESSION_KEY)).thenReturn(2L);
        when(stintRepository.countBySessionKey(SESSION_KEY)).thenReturn(2L);

        Page<LapTime> expectedPage = new PageImpl<>(List.of(lap1, lap2),
                PageRequest.of(0, 50), 2);
        when(lapTimeRepository.findBySessionKey(eq(SESSION_KEY), any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<LapTime> result = queryService.getLapsBySessionPaginated(SESSION_KEY, 0, 50);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("getLapsBySessionPaginated - ikinci səhifə - düzgün Pageable göndərilir")
    void getLapsBySessionPaginated_secondPage_correctPageableUsed() {
        when(lapTimeRepository.countBySessionKey(SESSION_KEY)).thenReturn(100L);
        when(stintRepository.countBySessionKey(SESSION_KEY)).thenReturn(0L);

        Page<LapTime> page = new PageImpl<>(List.of(lap2), PageRequest.of(1, 1), 100);
        when(lapTimeRepository.findBySessionKey(eq(SESSION_KEY), any(Pageable.class)))
                .thenReturn(page);

        Page<LapTime> result = queryService.getLapsBySessionPaginated(SESSION_KEY, 1, 1);

        assertThat(result.getNumber()).isEqualTo(1);
        verify(lapTimeRepository).findBySessionKey(eq(SESSION_KEY),
                argThat(p -> p.getPageNumber() == 1 && p.getPageSize() == 1));
    }

    // ─── getDriverLaps ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getDriverLaps - mövcud sürücü - lap sıralı qaydada qayıdır")
    void getDriverLaps_existingDriver_returnsOrderedLaps() {
        when(lapTimeRepository.findDriverLapsOrdered(SESSION_KEY, DRIVER_NUMBER))
                .thenReturn(List.of(lap1, lap2));

        List<LapTime> result = queryService.getDriverLaps(SESSION_KEY, DRIVER_NUMBER);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLapNumber()).isEqualTo(1);
        assertThat(result.get(1).getLapNumber()).isEqualTo(2);
        verify(lapTimeRepository).findDriverLapsOrdered(SESSION_KEY, DRIVER_NUMBER);
    }

    @Test
    @DisplayName("getDriverLaps - mövcud olmayan sürücü - boş siyahı qayıdır")
    void getDriverLaps_nonExistingDriver_returnsEmptyList() {
        when(lapTimeRepository.findDriverLapsOrdered(SESSION_KEY, 999))
                .thenReturn(List.of());

        List<LapTime> result = queryService.getDriverLaps(SESSION_KEY, 999);

        assertThat(result).isEmpty();
    }

    // ─── getFastestLaps ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getFastestLaps - laplar sürətə görə sıralanmış qayıdır")
    void getFastestLaps_returnsLapsOrderedByDuration() {
        // lap2 daha sürətlidir (92.456 < 95.123)
        when(lapTimeRepository.findFastestLapsBySession(SESSION_KEY))
                .thenReturn(List.of(lap2, lap1));

        List<LapTime> result = queryService.getFastestLaps(SESSION_KEY);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLapDuration()).isLessThan(result.get(1).getLapDuration());
    }

    @Test
    @DisplayName("getFastestLaps - boş session - boş siyahı qayıdır")
    void getFastestLaps_emptySession_returnsEmptyList() {
        when(lapTimeRepository.findFastestLapsBySession(SESSION_KEY))
                .thenReturn(List.of());

        List<LapTime> result = queryService.getFastestLaps(SESSION_KEY);

        assertThat(result).isEmpty();
    }

    // ─── getStintsBySession ───────────────────────────────────────────────────

    @Test
    @DisplayName("getStintsBySession - mövcud session - stint siyahısı qayıdır")
    void getStintsBySession_existingSession_returnsStints() {
        when(lapTimeRepository.countBySessionKey(SESSION_KEY)).thenReturn(0L);
        when(stintRepository.countBySessionKey(SESSION_KEY)).thenReturn(2L);
        when(stintRepository.findBySessionKey(SESSION_KEY)).thenReturn(List.of(stint1, stint2));

        List<Stint> result = queryService.getStintsBySession(SESSION_KEY);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCompound()).isEqualTo("MEDIUM");
        assertThat(result.get(1).getCompound()).isEqualTo("HARD");
    }

    @Test
    @DisplayName("getStintsBySession - mövcud olmayan session - SessionNotFoundException atılır")
    void getStintsBySession_nonExistingSession_throwsException() {
        when(lapTimeRepository.countBySessionKey(9999)).thenReturn(0L);
        when(stintRepository.countBySessionKey(9999)).thenReturn(0L);

        assertThatThrownBy(() -> queryService.getStintsBySession(9999))
                .isInstanceOf(SessionNotFoundException.class)
                .hasMessageContaining("9999");
    }

    // ─── getDriverStints ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getDriverStints - mövcud sürücü - stint siyahısı qayıdır")
    void getDriverStints_existingDriver_returnsStints() {
        when(stintRepository.findBySessionKeyAndDriverNumber(SESSION_KEY, 1))
                .thenReturn(List.of(stint1, stint2));

        List<Stint> result = queryService.getDriverStints(SESSION_KEY, 1);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStintNumber()).isEqualTo(1);
        assertThat(result.get(1).getStintNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("getDriverStints - mövcud olmayan sürücü - boş siyahı qayıdır")
    void getDriverStints_nonExistingDriver_returnsEmptyList() {
        when(stintRepository.findBySessionKeyAndDriverNumber(SESSION_KEY, 999))
                .thenReturn(List.of());

        List<Stint> result = queryService.getDriverStints(SESSION_KEY, 999);

        assertThat(result).isEmpty();
    }

    // ─── validateSessionExists (edge cases) ──────────────────────────────────

    @Test
    @DisplayName("validateSession - hər ikisi 0 olduqda exception atılır")
    void validateSession_bothCountsZero_throwsException() {
        when(lapTimeRepository.countBySessionKey(1)).thenReturn(0L);
        when(stintRepository.countBySessionKey(1)).thenReturn(0L);

        assertThatThrownBy(() -> queryService.getLapsBySession(1))
                .isInstanceOf(SessionNotFoundException.class)
                .hasMessageContaining("Session not found: 1");
    }
}
