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
public class SessionSummaryDTO {

    private Integer sessionKey;
    private Integer totalLapsRaced;
    private Integer numOvertakes;
    private String weatherCondition;
    private Double avgRacePace;
    private Integer safetyCarPeriods;
    private LocalDateTime generatedAt;
}
