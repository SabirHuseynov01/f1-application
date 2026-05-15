package com.example.f1timingservice.dto.openF1;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenF1StintDTO {

    @JsonProperty("session_key")
    private Integer sessionKey;

    @JsonProperty("driver_number")
    private Integer driverNumber;

    @JsonProperty("stint_number")
    private Integer stintNumber;

    @JsonProperty("lap_start")
    private Integer lapStart;

    @JsonProperty("lap_end")
    private Integer lapEnd;

    @JsonProperty("compound")
    private String compound;

    @JsonProperty("tyre_age_at_start")
    private Integer tyreAgeAtStart;
}
