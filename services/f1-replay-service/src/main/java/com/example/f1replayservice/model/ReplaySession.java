package com.example.f1replayservice.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "replay_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplaySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_session_key", nullable = false)
    private Integer originalSessionKey;

    @Column(name = "replay_name", nullable = false)
    private String replayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.CREATED;

    @Column(name = "current_time_offset")
    private Long currentTimeOffset = 0L; // milliseconds from start

    @Column(name = "speed_multiplier")
    private Double speedMultiplier = 1.0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum Status {
        CREATED, READY, PLAYING, PAUSED, COMPLETED, ERROR
    }
}
