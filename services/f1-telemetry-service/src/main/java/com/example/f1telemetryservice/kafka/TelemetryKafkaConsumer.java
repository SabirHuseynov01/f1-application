package com.example.f1telemetryservice.kafka;

import com.example.f1telemetryservice.dto.TelemetryDTO;
import com.example.f1telemetryservice.service.TelemetryProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelemetryKafkaConsumer {

    private final TelemetryProcessingService processingService;


    @KafkaListener(topics = "${kafka.topics.car-data}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeCarData(TelemetryDTO telemetry) {
        log.debug("Received car telemetry: session={}, driver={}",
                telemetry.getSessionKey(), telemetry.getDriverNumber());
        processingService.processCarTelemetry(telemetry);
    }
}
