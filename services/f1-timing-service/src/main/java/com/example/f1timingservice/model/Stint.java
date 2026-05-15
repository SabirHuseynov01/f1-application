package com.example.f1timingservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stints", indexes = {
        @Index(name = "idx_stint_session_driver", columnList = "session_key, driver_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_key", nullable = false)
    private Integer sessionKey;

    @Column(name = "driver_number", nullable = false)
    private Integer driverNumber;

    @Column(name = "stint_number")
    private Integer stintNumber;

    @Column(name = "lap_start")
    private Integer lapStart;

    @Column(name = "lap_end")
    private Integer lapEnd;

    @Column(name = "compound")
    private String compound;          // SOFT, MEDIUM, HARD, INTER, WET

    @Column(name = "tyre_age_at_start")
    private Integer tyreAgeAtStart;
}
