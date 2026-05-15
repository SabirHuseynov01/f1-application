package com.example.f1replayservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplayEventDTO {

    private String eventType;
    private Long eventTimeOffset;
    private Integer driverNumber;
    private String payload;
}
