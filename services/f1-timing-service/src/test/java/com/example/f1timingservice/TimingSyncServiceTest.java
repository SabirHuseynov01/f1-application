package com.example.f1timingservice;

import com.example.f1timingservice.client.OpenF1TimingClient;
import com.example.f1timingservice.dto.openF1.OpenF1LapDTO;
import com.example.f1timingservice.dto.openF1.OpenF1StintDTO;
import com.example.f1timingservice.model.LapTime;
import com.example.f1timingservice.model.Stint;
import com.example.f1timingservice.repository.LapTimeRepository;
import com.example.f1timingservice.repository.StintRepository;
import com.example.f1timingservice.service.TimingSyncService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimingSyncService Tests")
class TimingSyncServiceTest {

    @Mock
    private OpenF1TimingClient openF1TimingClient;

    @Mock
    private LapTimeRepository lapTimeRepository;

    @Mock
    private StintRepository stintRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TimingSyncService syncService;

    private static final Integer SESSION_KEY = 9158;

    private OpenF1LapDTO lapDTO1, lapDTO2;
    private OpenF1StintDTO stintDTO1, stintDTO2;

    @BeforeEach
    void setUp() {
        lapDTO1 = new OpenF1LapDTO(SESSION_KEY, 44, 1, 95.123, 28.1, 34.5, 32.4, false,
                List.of(2048, 2049), List.of(2049, 2052), List.of(2049, 2051));

        lapDTO2 = new OpenF1LapDTO(SESSION_KEY, 44, 2, 92.456, 27.3, 33.8, 31.3, false,
                List.of(2048), List.of(2049), List.of(2051));

        stintDTO1 = new OpenF1StintDTO(SESSION_KEY, 1, 1, 1, 22, "MEDIUM", 0);
        stintDTO2 = new OpenF1StintDTO(SESSION_KEY, 1, 2, 23, 50, "HARD", 3);
    }

    // ─── syncLapsBySession ────────────────────────────────────────────────────

    @Test
    @DisplayName("syncLapsBySession - mövcud olmayan laplar - hamısı saxlanılır")
    void syncLapsBySession_newLaps_savesAll() {
        when(openF1TimingClient.fetchLapsBySession(SESSION_KEY))
                .thenReturn(List.of(lapDTO1, lapDTO2));
        when(lapTimeRepository.findBySessionKeyAndDriverNumberAndLapNumber(
                SESSION_KEY, 44, 1)).thenReturn(Optional.empty());
        when(lapTimeRepository.findBySessionKeyAndDriverNumberAndLapNumber(
                SESSION_KEY, 44, 2)).thenReturn(Optional.empty());

        syncService.syncLapsBySession(SESSION_KEY);

        verify(lapTimeRepository, times(2)).save(any(LapTime.class));
    }

    @Test
    @DisplayName("syncLapsBySession - mövcud lap - saxlanılmır (skip olunur)")
    void syncLapsBySession_existingLap_skipsIt() {
        when(openF1TimingClient.fetchLapsBySession(SESSION_KEY))
                .thenReturn(List.of(lapDTO1));
        when(lapTimeRepository.findBySessionKeyAndDriverNumberAndLapNumber(
                SESSION_KEY, 44, 1))
                .thenReturn(Optional.of(LapTime.builder().id(1L).build()));

        syncService.syncLapsBySession(SESSION_KEY);

        verify(lapTimeRepository, never()).save(any());
    }

    @Test
    @DisplayName("syncLapsBySession - 1 yeni, 1 mövcud - yalnız yeni saxlanılır")
    void syncLapsBySession_oneNewOneExisting_savesOnlyNew() {
        when(openF1TimingClient.fetchLapsBySession(SESSION_KEY))
                .thenReturn(List.of(lapDTO1, lapDTO2));
        when(lapTimeRepository.findBySessionKeyAndDriverNumberAndLapNumber(SESSION_KEY, 44, 1))
                .thenReturn(Optional.empty());
        when(lapTimeRepository.findBySessionKeyAndDriverNumberAndLapNumber(SESSION_KEY, 44, 2))
                .thenReturn(Optional.of(LapTime.builder().id(2L).build()));

        syncService.syncLapsBySession(SESSION_KEY);

        verify(lapTimeRepository, times(1)).save(any(LapTime.class));
    }

    @Test
    @DisplayName("syncLapsBySession - boş cavab - heç nə saxlanılmır")
    void syncLapsBySession_emptyResponse_savesNothing() {
        when(openF1TimingClient.fetchLapsBySession(SESSION_KEY))
                .thenReturn(List.of());

        syncService.syncLapsBySession(SESSION_KEY);

        verify(lapTimeRepository, never()).save(any());
    }

    @Test
    @DisplayName("syncLapsBySession - lap entity-nin sahələri düzgün map olunur")
    void syncLapsBySession_lapFields_mappedCorrectly() throws Exception {
        when(openF1TimingClient.fetchLapsBySession(SESSION_KEY))
                .thenReturn(List.of(lapDTO1));
        when(lapTimeRepository.findBySessionKeyAndDriverNumberAndLapNumber(
                SESSION_KEY, 44, 1)).thenReturn(Optional.empty());
        when(objectMapper.writeValueAsString(any())).thenReturn("[2048,2049]");

        syncService.syncLapsBySession(SESSION_KEY);

        ArgumentCaptor<LapTime> captor = ArgumentCaptor.forClass(LapTime.class);
        verify(lapTimeRepository).save(captor.capture());

        LapTime saved = captor.getValue();
        assertThat(saved.getSessionKey()).isEqualTo(SESSION_KEY);
        assertThat(saved.getDriverNumber()).isEqualTo(44);
        assertThat(saved.getLapNumber()).isEqualTo(1);
        assertThat(saved.getLapDuration()).isEqualTo(95.123);
        assertThat(saved.getSector1Duration()).isEqualTo(28.1);
        assertThat(saved.getSector2Duration()).isEqualTo(34.5);
        assertThat(saved.getSector3Duration()).isEqualTo(32.4);
        assertThat(saved.getIsPitOutLap()).isFalse();
    }

    @Test
    @DisplayName("syncLapsBySession - isPitOutLap true olan lap - düzgün map olunur")
    void syncLapsBySession_pitOutLap_mappedCorrectly() {
        OpenF1LapDTO pitLap = new OpenF1LapDTO(SESSION_KEY, 11, 5, 110.0,
                null, null, null, true, List.of(), List.of(), List.of());

        when(openF1TimingClient.fetchLapsBySession(SESSION_KEY))
                .thenReturn(List.of(pitLap));
        when(lapTimeRepository.findBySessionKeyAndDriverNumberAndLapNumber(SESSION_KEY, 11, 5))
                .thenReturn(Optional.empty());

        syncService.syncLapsBySession(SESSION_KEY);

        ArgumentCaptor<LapTime> captor = ArgumentCaptor.forClass(LapTime.class);
        verify(lapTimeRepository).save(captor.capture());
        assertThat(captor.getValue().getIsPitOutLap()).isTrue();
    }

    // ─── syncStintsBySession ──────────────────────────────────────────────────

    @Test
    @DisplayName("syncStintsBySession - mövcud olmayan stintlər - hamısı saxlanılır")
    void syncStintsBySession_newStints_savesAll() {
        when(openF1TimingClient.fetchStintsBySession(SESSION_KEY))
                .thenReturn(List.of(stintDTO1, stintDTO2));
        when(stintRepository.findBySessionKeyAndDriverNumberAndStintNumber(
                SESSION_KEY, 1, 1)).thenReturn(Optional.empty());
        when(stintRepository.findBySessionKeyAndDriverNumberAndStintNumber(
                SESSION_KEY, 1, 2)).thenReturn(Optional.empty());

        syncService.syncStintsBySession(SESSION_KEY);

        verify(stintRepository, times(2)).save(any(Stint.class));
    }

    @Test
    @DisplayName("syncStintsBySession - mövcud stint - saxlanılmır")
    void syncStintsBySession_existingStint_skipsIt() {
        when(openF1TimingClient.fetchStintsBySession(SESSION_KEY))
                .thenReturn(List.of(stintDTO1));
        when(stintRepository.findBySessionKeyAndDriverNumberAndStintNumber(
                SESSION_KEY, 1, 1))
                .thenReturn(Optional.of(Stint.builder().id(1L).build()));

        syncService.syncStintsBySession(SESSION_KEY);

        verify(stintRepository, never()).save(any());
    }

    @Test
    @DisplayName("syncStintsBySession - stint entity-nin sahələri düzgün map olunur")
    void syncStintsBySession_stintFields_mappedCorrectly() {
        when(openF1TimingClient.fetchStintsBySession(SESSION_KEY))
                .thenReturn(List.of(stintDTO1));
        when(stintRepository.findBySessionKeyAndDriverNumberAndStintNumber(
                SESSION_KEY, 1, 1)).thenReturn(Optional.empty());

        syncService.syncStintsBySession(SESSION_KEY);

        ArgumentCaptor<Stint> captor = ArgumentCaptor.forClass(Stint.class);
        verify(stintRepository).save(captor.capture());

        Stint saved = captor.getValue();
        assertThat(saved.getSessionKey()).isEqualTo(SESSION_KEY);
        assertThat(saved.getDriverNumber()).isEqualTo(1);
        assertThat(saved.getStintNumber()).isEqualTo(1);
        assertThat(saved.getLapStart()).isEqualTo(1);
        assertThat(saved.getLapEnd()).isEqualTo(22);
        assertThat(saved.getCompound()).isEqualTo("MEDIUM");
        assertThat(saved.getTyreAgeAtStart()).isEqualTo(0);
    }

    @Test
    @DisplayName("syncStintsBySession - boş cavab - heç nə saxlanılmır")
    void syncStintsBySession_emptyResponse_savesNothing() {
        when(openF1TimingClient.fetchStintsBySession(SESSION_KEY))
                .thenReturn(List.of());

        syncService.syncStintsBySession(SESSION_KEY);

        verify(stintRepository, never()).save(any());
    }

    // ─── syncSessionAsync ─────────────────────────────────────────────────────

    @Test
    @DisplayName("syncSessionAsync - həm lap, həm stint sync çağrılır")
    void syncSessionAsync_callsBothSyncMethods() {
        when(openF1TimingClient.fetchLapsBySession(SESSION_KEY)).thenReturn(List.of());
        when(openF1TimingClient.fetchStintsBySession(SESSION_KEY)).thenReturn(List.of());

        CompletableFuture<Void> future = syncService.syncSessionAsync(SESSION_KEY);

        assertThat(future).isNotNull();
        assertThat(future.isDone()).isTrue();
        verify(openF1TimingClient).fetchLapsBySession(SESSION_KEY);
        verify(openF1TimingClient).fetchStintsBySession(SESSION_KEY);
    }
}
