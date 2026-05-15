package com.example.f1telemetryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryDTO {

    private Integer sessionKey;
    private Integer driverNumber;
    private Double speed;
    private Integer rpm;
    private Integer gear;
    private Double throttle;
    private Double brake;
    private Double drs;
    private LocalDateTime timestamp;
}
