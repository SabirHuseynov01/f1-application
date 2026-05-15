package com.example.f1notificationservice;

import com.example.f1notificationservice.dto.NotificationRequestDTO;
import com.example.f1notificationservice.kafka.NotificationKafkaConsumer;
import com.example.f1notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationKafkaConsumer Tests")
class NotificationKafkaConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationKafkaConsumer consumer;

    private NotificationRequestDTO eventDTO;

    @BeforeEach
    void setUp() {
        eventDTO = new NotificationRequestDTO();
        eventDTO.setType("RACE_START");
        eventDTO.setTitle("Race Started");
        eventDTO.setMessage("The race has begun!");
        eventDTO.setSessionKey(9158);
    }

    @Test
    @DisplayName("consumeRaceEvent - processes race event")
    void consumeRaceEvent_processesEvent() {
        consumer.consumeRaceEvent(eventDTO);

        verify(notificationService).broadcastNotification(eventDTO);
    }

    @Test
    @DisplayName("consumeAlert - processes alert")
    void consumeAlert_processesAlert() {
        eventDTO.setType("PENALTY");
        eventDTO.setTitle("Penalty Alert");

        consumer.consumeAlert(eventDTO);

        verify(notificationService).sendUrgentNotification(eventDTO);
    }
}