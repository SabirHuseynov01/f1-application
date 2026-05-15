package com.example.f1replayservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplayControlDTO {

    private Long replaySessionId;
    private String action; // PLAY, PAUSE, STOP, SEEK, SPEED
    private Long seekOffset; // for SEEK action
    private Double speed; // for SPEED action
}
