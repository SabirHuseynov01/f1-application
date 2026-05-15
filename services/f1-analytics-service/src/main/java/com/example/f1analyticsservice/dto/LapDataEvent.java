package com.example.f1analyticsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LapDataEvent {

    private Integer sessionKey;
    private Integer driverNumber;
    private Integer lapNumber;
    private Double lapDuration;
    private Double speed;
}
