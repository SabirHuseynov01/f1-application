package com.example.f1sessionservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncResultDTO {
    private Integer year;
    private int sessionsCreated;
    private int sessionsSkipped;
    private int driversCreated;
    private String status;
}
