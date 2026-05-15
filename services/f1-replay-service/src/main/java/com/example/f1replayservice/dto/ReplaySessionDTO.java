package com.example.f1replayservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplaySessionDTO {

    private Long id;
    private Integer originalSessionKey;
    private String replayName;
    private String status;
    private Long currentTimeOffset;
    private Double speedMultiplier;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
}
