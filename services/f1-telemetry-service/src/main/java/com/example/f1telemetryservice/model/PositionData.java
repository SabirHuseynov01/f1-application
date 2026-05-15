package com.example.f1telemetryservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "position_data", indexes = {
        @Index(name = "idx_pd_session_driver", columnList = "session_key,driver_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_key", nullable = false)
    private Integer sessionKey;

    @Column(name = "driver_number", nullable = false)
    private Integer driverNumber;

    private Double x;
    private Double y;
    private Double z;

    @Column(name = "lap_distance")
    private Double lapDistance;

    @Column(name = "total_distance")
    private Double totalDistance;

    private LocalDateTime timestamp;


}
