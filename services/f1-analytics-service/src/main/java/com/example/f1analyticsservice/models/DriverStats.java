package com.example.f1analyticsservice.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "driver_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_key", nullable = false)
    private Integer sessionKey;

    @Column(name = "driver_number", nullable = false)
    private Integer driverNumber;

    @Column(name = "total_laps")
    private Integer totalLaps;

    @Column(name = "fastest_lap_time")
    private Double fastestLapTime;

    @Column(name = "avg_lap_time")
    private Double avgLapTime;

    @Column(name = "total_pit_stops")
    private Integer totalPitStops;

    @Column(name = "total_distance")
    private Double totalDistance;

    @Column(name = "max_speed")
    private Double maxSpeed;

    @Column(name = "avg_speed")
    private Double avgSpeed;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @PrePersist
    protected void onCreate() {
        calculatedAt = LocalDateTime.now();
    }
}
