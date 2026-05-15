package com.example.f1telemetryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetrySnapshotDTO {

    private Integer sessionKey;
    private List<DriverTelemetryDTO> drivers;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverTelemetryDTO {
        private Integer driverNumber;
        private Double speed;
        private Integer gear;
        private Double throttle;
        private Double drs;
        private PositionDTO position;
    }
}
