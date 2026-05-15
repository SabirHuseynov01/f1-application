package com.example.f1timingservice.dto.openF1;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenF1LapDTO {

    @JsonProperty("session_key")
    private Integer sessionKey;

    @JsonProperty("driver_number")
    private Integer driverNumber;

    @JsonProperty("lap_number")
    private Integer lapNumber;

    @JsonProperty("lap_duration")
    private Double lapDuration;

    @JsonProperty("duration_sector_1")
    private Double durationSector1;

    @JsonProperty("duration_sector_2")
    private Double durationSector2;

    @JsonProperty("duration_sector_3")
    private Double durationSector3;

    @JsonProperty("is_pit_out_lap")
    private Boolean isPitOutLap;

    @JsonProperty("segments_sector_1")
    private List<Integer> segmentsSector1;

    @JsonProperty("segments_sector_2")
    private List<Integer> segmentsSector2;

    @JsonProperty("segments_sector_3")
    private List<Integer> segmentsSector3;
}
