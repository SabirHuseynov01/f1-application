package com.example.f1sessionservice.dto.openF1;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenF1DriverDTO {

    @NotNull
    @JsonProperty("driver_number")
    private Integer driverNumber;

    @NotNull
    @JsonProperty("name_acronym")
    private String nameAcronym;   // "VER", "RUS", "PIA"

    @NotNull
    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("team_name")
    private String teamName;

    @JsonProperty("team_colour")
    private String teamColour;

    @NotNull
    @JsonProperty("session_key")
    private Integer sessionKey;
}
