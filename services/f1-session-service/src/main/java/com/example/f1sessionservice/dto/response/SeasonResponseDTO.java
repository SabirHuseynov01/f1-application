package com.example.f1sessionservice.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonResponseDTO {
    private Long id;
    private Integer year;
    private String championshipName;
}
