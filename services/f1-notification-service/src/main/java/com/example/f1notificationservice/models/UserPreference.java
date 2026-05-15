package com.example.f1notificationservice.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    private boolean emailNotifications = true;
    private boolean pushNotifications = true;
    private boolean raceStartAlert = true;
    private boolean fastestLapAlert = true;
    private boolean podiumAlert = true;
    private boolean penaltyAlert = true;
    private boolean weatherAlert = false;
}
