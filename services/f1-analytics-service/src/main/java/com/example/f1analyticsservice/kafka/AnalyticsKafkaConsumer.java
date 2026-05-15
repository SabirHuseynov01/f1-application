package com.example.f1analyticsservice.kafka;

import com.example.f1analyticsservice.dto.LapDataEvent;
import com.example.f1analyticsservice.service.AnalyticsCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsKafkaConsumer {

    private final AnalyticsCalculationService calculationService;

    @KafkaListener(topics = "${kafka.topics.laps}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeLapData(LapDataEvent lap) {
        log.debug("Processing lap data for analytics: session={}, driver={}, lap={}",
                lap.getSessionKey(), lap.getDriverNumber(), lap.getLapNumber());
        calculationService.processLapData(lap);
    }
}
