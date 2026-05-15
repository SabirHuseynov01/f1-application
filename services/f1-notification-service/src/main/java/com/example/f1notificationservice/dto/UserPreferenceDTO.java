package com.example.f1notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPreferenceDTO {

    private Long userId;
    private boolean emailNotifications;
    private boolean pushNotifications;
    private boolean raceStartAlert;
    private boolean fastestLapAlert;
    private boolean podiumAlert;
    private boolean penaltyAlert;
    private boolean weatherAlert;
}
