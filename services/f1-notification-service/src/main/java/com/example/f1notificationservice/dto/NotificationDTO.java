package com.example.f1notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;
    private String type;
    private String title;
    private String message;
    private Integer sessionKey;
    private String status;
    private LocalDateTime createdAt;
}
