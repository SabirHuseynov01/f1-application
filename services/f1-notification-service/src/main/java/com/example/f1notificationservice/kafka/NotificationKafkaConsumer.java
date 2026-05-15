package com.example.f1notificationservice.kafka;

import com.example.f1notificationservice.dto.NotificationRequestDTO;
import com.example.f1notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "${kafka.topics.race-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeRaceEvent(NotificationRequestDTO event) {
        log.info("Received race event: {} - {}", event.getType(), event.getTitle());
        notificationService.broadcastNotification(event);
    }

    @KafkaListener(topics = "${kafka.topics.alerts}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeAlert(NotificationRequestDTO alert) {
        log.warn("Received alert: {} - {}", alert.getType(), alert.getTitle());
        notificationService.sendUrgentNotification(alert);
    }
}
