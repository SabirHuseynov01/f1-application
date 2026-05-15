package com.example.f1replayservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "replay_events", indexes = {
        @Index(name = "idx_replay_time", columnList = "replay_session_id,event_time_offset")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplayEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replay_session_id", nullable = false)
    private ReplaySession replaySession;

    @Column(name = "event_type", nullable = false)
    private String eventType; // LAP_TIME, POSITION, TELEMETRY, PIT_STOP, OVERTAKE

    @Column(name = "event_time_offset", nullable = false)
    private Long eventTimeOffset; // ms from session start

    @Column(name = "driver_number")
    private Integer driverNumber;

    @Column(name = "payload", length = 4000)
    private String payload; // JSON data

    @Column(name = "sequence_number")
    private Integer sequenceNumber;
}
