package com.example.f1sessionservice.dto.openF1;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenF1SessionDTO {

    @NotNull
    @JsonProperty("session_key")
    private Integer sessionKey;

    @NotNull
    @JsonProperty("session_name")
    private String sessionName; // "FP1", "FP2", "FP3", "SPRINT", "SPRINT_QUALIFYING", "QUALIFYING", "RACE"

    @JsonProperty("session_type")
    private String sessionType;

    @JsonProperty("date_start")
    private OffsetDateTime dateStart;

    @JsonProperty("date_end")
    private OffsetDateTime dateEnd;

    @NotNull
    @JsonProperty("year")
    private Integer year;

    @JsonProperty("circuit_key")
    private Integer circuitKey;

    @JsonProperty("circuit_short_name")
    private String circuitShortName;

    @JsonProperty("country_name")
    private String countryName;

    @JsonProperty("location")
    private String location;

}
