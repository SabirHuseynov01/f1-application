package com.example.f1analyticsservice.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "session_summaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_key", unique = true, nullable = false)
    private Integer sessionKey;

    @Column(name = "total_laps_raced")
    private Integer totalLapsRaced;

    @Column(name = "num_overtakes")
    private Integer numOvertakes;

    @Column(name = "weather_condition")
    private String weatherCondition;

    @Column(name = "avg_race_pace")
    private Double avgRacePace;

    @Column(name = "safety_car_periods")
    private Integer safetyCarPeriods;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
    }
}
