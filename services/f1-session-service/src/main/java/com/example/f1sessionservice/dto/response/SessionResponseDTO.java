package com.example.f1sessionservice.dto.response;

import com.example.f1sessionservice.model.SessionType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResponseDTO {
    private Long id;
    private Integer sessionKey;
    private Integer seasonYear;
    private String circuitName;
    private String country;
    private String city;
    private SessionType sessionType;
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;
}