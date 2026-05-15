package com.example.f1notificationservice.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {


    @NotBlank
    private String type;

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    private Integer sessionKey;

    private Long userId;

}
