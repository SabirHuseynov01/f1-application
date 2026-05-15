package com.example.f1timingservice.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StintResponseDTO {

    private Long id;
    private Integer sessionKey;
    private Integer driverNumber;
    private Integer stintNumber;
    private Integer lapStart;
    private Integer lapEnd;
    private String compound;
    private Integer tyreAgeAtStart;
}
