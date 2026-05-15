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
public class PositionDTO {

    private Integer sessionKey;
    private Integer driverNumber;
    private Double x;
    private Double y;
    private Double z;
    private Double lapDistance;
    private LocalDateTime timestamp;
}
