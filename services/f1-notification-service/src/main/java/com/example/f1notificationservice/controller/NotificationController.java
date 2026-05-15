package com.example.f1notificationservice.controller;

import com.example.f1notificationservice.dto.NotificationDTO;
import com.example.f1notificationservice.dto.NotificationRequestDTO;
import com.example.f1notificationservice.dto.UserPreferenceDTO;
import com.example.f1notificationservice.models.Notification;
import com.example.f1notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<NotificationDTO>> getUnread(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "UNREAD") Notification.Status status) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId, status));
    }

    @GetMapping("/{userId}/all")
    public ResponseEntity<?> getAll(@PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.ok(notificationService.getUserNotificationsPaginated(userId,pageable));
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/preferences/{userId}")
    public ResponseEntity<UserPreferenceDTO> updatePreferences(
            @PathVariable Long userId,
            @Valid @RequestBody UserPreferenceDTO dto) {
        return ResponseEntity.ok(notificationService.updatePreferences(userId, dto));
    }

    @PostMapping("/broadcast")
    public ResponseEntity<Void> broadcast(@Valid @RequestBody NotificationRequestDTO request) {
        notificationService.broadcastNotification(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("notification-service UP");
    }
}
