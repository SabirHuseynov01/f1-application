package com.example.f1timingservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lap_times", indexes = {
        @Index(name = "idx_session_driver", columnList = "session_key, driver_number"),
        @Index(name = "idx_session_lap", columnList = "session_key, lap_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LapTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_key", nullable = false)
    private Integer sessionKey;

    @Column(name = "driver_number", nullable = false)
    private Integer driverNumber;

    @Column(name = "lap_number", nullable = false)
    private Integer lapNumber;

    @Column(name = "lap_duration")
    private Double lapDuration;       // saniyə — 83.456

    @Column(name = "sector1_duration")
    private Double sector1Duration;

    @Column(name = "sector2_duration")
    private Double sector2Duration;

    @Column(name = "sector3_duration")
    private Double sector3Duration;

    @Column(name = "is_pit_out_lap")
    private Boolean isPitOutLap;

    @Column(name = "segments_sector1", length = 500)
    private String segmentsSector1;

    @Column(name = "segments_sector2", length = 500)
    private String segmentsSector2;

    @Column(name = "segments_sector3", length = 500)
    private String segmentsSector3;
}
