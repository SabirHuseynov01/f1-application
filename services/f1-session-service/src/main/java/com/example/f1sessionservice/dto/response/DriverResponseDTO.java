package com.example.f1sessionservice.dto.response;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverResponseDTO {
    private Long id;
    private Integer driverNumber;
    private String driverCode;
    private String fullName;
    private String teamName;
    private String teamColour;
}
