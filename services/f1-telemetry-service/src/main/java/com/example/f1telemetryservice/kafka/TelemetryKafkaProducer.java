package com.example.f1telemetryservice.kafka;

import com.example.f1telemetryservice.dto.TelemetryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelemetryKafkaProducer {

    private final KafkaTemplate<String, TelemetryDTO> kafkaTemplate;

    @Value("${kafka.topics.telemetry}")
    private String telemetryTopic;

    public void sendTelemetry(TelemetryDTO telemetry) {
        String key = telemetry.getSessionKey() + "-" + telemetry.getDriverNumber();
        kafkaTemplate.send(telemetryTopic, key, telemetry)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send telemetry: {}", ex.getMessage());
                    }
                });
    }
}
