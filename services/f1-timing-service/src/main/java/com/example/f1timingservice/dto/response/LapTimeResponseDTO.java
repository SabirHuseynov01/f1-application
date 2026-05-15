package com.example.f1timingservice.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LapTimeResponseDTO {

    private Long id;
    private Integer sessionKey;
    private Integer driverNumber;
    private Integer lapNumber;
    private Double lapDuration;
    private Double sector1Duration;
    private Double sector2Duration;
    private Double sector3Duration;
    private Boolean isPitOutLap;
}
