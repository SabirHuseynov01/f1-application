package com.example.f1telemetryservice;

import com.example.f1telemetryservice.dto.TelemetryDTO;
import com.example.f1telemetryservice.model.CarTelemetry;
import com.example.f1telemetryservice.repository.CarTelemetryRepository;
import com.example.f1telemetryservice.service.TelemetryProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TelemetryProcessingService Tests")
class TelemetryProcessingServiceTest {

    @Mock
    private CarTelemetryRepository carTelemetryRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private TelemetryProcessingService processingService;

    private TelemetryDTO telemetryDTO;

    @BeforeEach
    void setUp() {
        telemetryDTO = TelemetryDTO.builder()
                .sessionKey(9158)
                .driverNumber(1)
                .speed(285.5)
                .rpm(10500)
                .gear(7)
                .throttle(100.0)
                .brake(0.0)
                .drs(1.0)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("processCarTelemetry - saves telemetry and sends WebSocket message")
    void processCarTelemetry_savesAndBroadcasts() {
        when(carTelemetryRepository.save(any(CarTelemetry.class))).thenAnswer(inv -> inv.getArgument(0));

        processingService.processCarTelemetry(telemetryDTO);

        ArgumentCaptor<CarTelemetry> captor = ArgumentCaptor.forClass(CarTelemetry.class);
        verify(carTelemetryRepository).save(captor.capture());

        CarTelemetry saved = captor.getValue();
        assertThat(saved.getSessionKey()).isEqualTo(9158);
        assertThat(saved.getDriverNumber()).isEqualTo(1);
        assertThat(saved.getSpeed()).isEqualTo(285.5);
        assertThat(saved.getRpm()).isEqualTo(10500);
        assertThat(saved.getGear()).isEqualTo(7);
        assertThat(saved.getThrottle()).isEqualTo(100.0);
        assertThat(saved.getBrake()).isEqualTo(0.0);
        assertThat(saved.getDrs()).isEqualTo(1.0);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/telemetry/9158/1"),
                eq(telemetryDTO)
        );
    }

    @Test
    @DisplayName("processCarTelemetry - handles null values gracefully")
    void processCarTelemetry_nullValues_savesWithNulls() {
        TelemetryDTO dtoWithNulls = TelemetryDTO.builder()
                .sessionKey(9158)
                .driverNumber(1)
                .timestamp(LocalDateTime.now())
                .build();

        when(carTelemetryRepository.save(any(CarTelemetry.class))).thenAnswer(inv -> inv.getArgument(0));

        processingService.processCarTelemetry(dtoWithNulls);

        ArgumentCaptor<CarTelemetry> captor = ArgumentCaptor.forClass(CarTelemetry.class);
        verify(carTelemetryRepository).save(captor.capture());

        CarTelemetry saved = captor.getValue();
        assertThat(saved.getSpeed()).isNull();
        assertThat(saved.getRpm()).isNull();
        assertThat(saved.getGear()).isNull();
        assertThat(saved.getThrottle()).isNull();
        assertThat(saved.getBrake()).isNull();
        assertThat(saved.getDrs()).isNull();
        assertThat(saved.getSessionKey()).isEqualTo(9158);
        assertThat(saved.getDriverNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("processCarTelemetry - sends to correct WebSocket topic")
    void processCarTelemetry_correctWebSocketTopic() {
        when(carTelemetryRepository.save(any(CarTelemetry.class))).thenAnswer(inv -> inv.getArgument(0));

        processingService.processCarTelemetry(telemetryDTO);

        verify(messagingTemplate).convertAndSend(
                "/topic/telemetry/9158/1",
                telemetryDTO
        );
    }

    @Test
    @DisplayName("processCarTelemetry - different session and driver creates different topics")
    void processCarTelemetry_differentTopics() {
        TelemetryDTO dto2 = TelemetryDTO.builder()
                .sessionKey(9523)
                .driverNumber(44)
                .speed(290.0)
                .timestamp(LocalDateTime.now())
                .build();

        when(carTelemetryRepository.save(any(CarTelemetry.class))).thenAnswer(inv -> inv.getArgument(0));

        processingService.processCarTelemetry(telemetryDTO);
        processingService.processCarTelemetry(dto2);

        verify(messagingTemplate).convertAndSend("/topic/telemetry/9158/1", telemetryDTO);
        verify(messagingTemplate).convertAndSend("/topic/telemetry/9523/44", dto2);
    }
}
