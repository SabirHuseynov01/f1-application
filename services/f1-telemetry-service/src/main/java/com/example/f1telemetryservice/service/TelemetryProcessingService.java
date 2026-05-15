package com.example.f1telemetryservice.service;

import com.example.f1telemetryservice.dto.TelemetryDTO;
import com.example.f1telemetryservice.model.CarTelemetry;
import com.example.f1telemetryservice.repository.CarTelemetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryProcessingService {

    private final CarTelemetryRepository carTelemetryRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void processCarTelemetry(TelemetryDTO dto) {
        CarTelemetry telemetry = CarTelemetry.builder()
                .sessionKey(dto.getSessionKey())
                .driverNumber(dto.getDriverNumber())
                .speed(dto.getSpeed())
                .rpm(dto.getRpm())
                .gear(dto.getGear())
                .throttle(dto.getThrottle())
                .brake(dto.getBrake())
                .drs(dto.getDrs())
                .timestamp(dto.getTimestamp())
                .build();

        carTelemetryRepository.save(telemetry);

        String destination = String.format("/topic/telemetry/%d/%d",
                dto.getSessionKey(), dto.getDriverNumber());
        messagingTemplate.convertAndSend(destination, dto);
    }
}
