package com.example.f1telemetryservice;

import com.example.f1telemetryservice.dto.TelemetryDTO;
import com.example.f1telemetryservice.kafka.TelemetryKafkaConsumer;
import com.example.f1telemetryservice.service.TelemetryProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TelemetryKafkaConsumer Tests")
class TelemetryKafkaConsumerTest {

    @Mock
    private TelemetryProcessingService processingService;

    @InjectMocks
    private TelemetryKafkaConsumer consumer;

    private TelemetryDTO telemetryDTO;

    @BeforeEach
    void setUp() {
        telemetryDTO = TelemetryDTO.builder()
                .sessionKey(9158)
                .driverNumber(1)
                .speed(285.5)
                .rpm(10500)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("consumeCarData - processes telemetry message")
    void consumeCarData_processesTelemetry() {
        consumer.consumeCarData(telemetryDTO);

        verify(processingService).processCarTelemetry(telemetryDTO);
    }

    @Test
    @DisplayName("consumeCarData - handles null speed gracefully")
    void consumeCarData_nullSpeed_processes() {
        TelemetryDTO dtoWithNullSpeed = TelemetryDTO.builder()
                .sessionKey(9158)
                .driverNumber(1)
                .timestamp(LocalDateTime.now())
                .build();

        consumer.consumeCarData(dtoWithNullSpeed);

        verify(processingService).processCarTelemetry(dtoWithNullSpeed);
    }
}
