package com.example.f1analyticsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverStatsDTO {

    private Integer sessionKey;
    private Integer driverNumber;
    private Integer totalLaps;
    private Double fastestLapTime;
    private Double avgLapTime;
    private Integer totalPitStops;
    private Double maxSpeed;
    private Double avgSpeed;
    private LocalDateTime calculatedAt;
}
