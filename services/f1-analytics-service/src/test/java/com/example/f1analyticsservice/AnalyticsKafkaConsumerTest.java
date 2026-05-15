package com.example.f1analyticsservice;

import com.example.f1analyticsservice.dto.LapDataEvent;
import com.example.f1analyticsservice.kafka.AnalyticsKafkaConsumer;
import com.example.f1analyticsservice.service.AnalyticsCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsKafkaConsumer Tests")
class AnalyticsKafkaConsumerTest {

    @Mock
    private AnalyticsCalculationService calculationService;

    @InjectMocks
    private AnalyticsKafkaConsumer consumer;

    private LapDataEvent lapDataEvent;

    @BeforeEach
    void setUp() {
        lapDataEvent = new LapDataEvent();
        lapDataEvent.setSessionKey(9158);
        lapDataEvent.setDriverNumber(1);
        lapDataEvent.setLapNumber(5);
        lapDataEvent.setLapDuration(93.456);
        lapDataEvent.setSpeed(285.5);
    }

    @Test
    @DisplayName("consumeLapData - processes lap data event")
    void consumeLapData_processesEvent() {
        consumer.consumeLapData(lapDataEvent);

        verify(calculationService).processLapData(lapDataEvent);
    }

    @Test
    @DisplayName("consumeLapData - handles event with null speed")
    void consumeLapData_nullSpeed_processes() {
        lapDataEvent.setSpeed(null);

        consumer.consumeLapData(lapDataEvent);

        verify(calculationService).processLapData(lapDataEvent);
    }
}
