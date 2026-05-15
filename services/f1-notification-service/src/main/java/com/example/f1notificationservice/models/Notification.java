package com.example.f1notificationservice.models;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private String type; // RACE_START, FASTEST_LAP, PODIUM, PENALTY, WEATHER

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "session_key")
    private Integer sessionKey;

    @Enumerated(EnumType.STRING)
    private Status status = Status.UNREAD;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void setReadAt(LocalDateTime now) {
        this.status = Status.READ;
    }


    public enum Status {
        UNREAD, READ, ARCHIVED
    }
}
