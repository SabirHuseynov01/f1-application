package com.example.f1replayservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplayFrameDTO {

    private Long replaySessionId;
    private Long currentOffset;
    private List<ReplayEventDTO> events;
    private boolean isLastFrame;
}
