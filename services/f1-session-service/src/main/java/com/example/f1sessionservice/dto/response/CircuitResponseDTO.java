package com.example.f1sessionservice.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CircuitResponseDTO {

    private Long id;
    private Integer circuitKey;
    private String name;
    private String country;
    private String city;
    private Double trackLengthKm;
    private Integer corners;
}
