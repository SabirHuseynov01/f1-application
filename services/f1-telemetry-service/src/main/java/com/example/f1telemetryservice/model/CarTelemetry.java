package com.example.f1telemetryservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "car_telemetry", indexes = {
        @Index(name = "idx_ct_session_number", columnList = "session_key,driver_number"),
        @Index(name = "idx_ct_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarTelemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_key", nullable = false)
    private Integer sessionKey;

    @Column(name = "driver_number", nullable = false)
    private Integer driverNumber;

    private Double speed;           // km/h
    private Integer rpm;
    private Integer gear;
    private Double throttle;        // 0-100
    private Double brake;           // 0-100
    private Double drs;             // 0-1


    @Column(name = "engine_temp")
    private Double engineTemp;

    @Column(name = "tire_temp_fl")
    private Double tireTempFL;

    @Column(name = "tire_temp_fr")
    private Double tireTempFR;

    @Column(name = "tire_temp_rl")
    private Double tireTempRL;

    @Column(name = "tire_temp_rr")
    private Double tireTempRR;

    private LocalDateTime timestamp;
}
