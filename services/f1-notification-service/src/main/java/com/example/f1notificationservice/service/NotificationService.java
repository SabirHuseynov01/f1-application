package com.example.f1notificationservice.service;

import com.example.f1notificationservice.dto.NotificationDTO;
import com.example.f1notificationservice.dto.NotificationRequestDTO;
import com.example.f1notificationservice.dto.UserPreferenceDTO;
import com.example.f1notificationservice.models.Notification;
import com.example.f1notificationservice.models.UserPreference;
import com.example.f1notificationservice.repository.NotificationRepository;
import com.example.f1notificationservice.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final JavaMailSender mailSender;

    @Transactional
    public void broadcastNotification(NotificationRequestDTO request) {
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .sessionKey(request.getSessionKey())
                .status(Notification.Status.UNREAD)
                .build();

        notificationRepository.save(notification);

        // WebSocket push
        messagingTemplate.convertAndSend("/topic/notifications",
                mapToDTO(notification));

        // Email if enabled
        if (request.getUserId() != null) {
            preferenceRepository.findByUserId(request.getUserId())
                    .ifPresent(pref -> {
                        if (pref.isEmailNotifications() && isAlertEnabled(pref, request.getType())) {
                            sendEmail(request);
                        }
                    });
        }
    }

    @Transactional
    public void sendUrgentNotification(NotificationRequestDTO request) {
        broadcastNotification(request);
        // Additional urgent handling - push to specific user WebSocket
        if (request.getUserId() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/notifications/" + request.getUserId(),
                    mapToDTO(Notification.builder()
                            .type(request.getType())
                            .title(request.getTitle())
                            .message(request.getMessage())
                            .build()));
        }
    }

    public List<NotificationDTO> getUserNotifications(Long userId, Notification.Status status) {
        return notificationRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(userId, status)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Page<NotificationDTO> getUserNotificationsPaginated(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToDTO);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setStatus(Notification.Status.READ);
            n.setReadAt(java.time.LocalDateTime.now());
        });
    }

    @Transactional
    public UserPreferenceDTO updatePreferences(Long userId, UserPreferenceDTO dto) {
        UserPreference pref = preferenceRepository.findByUserId(userId)
                .orElse(UserPreference.builder().userId(userId).build());

        pref.setEmailNotifications(dto.isEmailNotifications());
        pref.setPushNotifications(dto.isPushNotifications());
        pref.setRaceStartAlert(dto.isRaceStartAlert());
        pref.setFastestLapAlert(dto.isFastestLapAlert());
        pref.setPodiumAlert(dto.isPodiumAlert());
        pref.setPenaltyAlert(dto.isPenaltyAlert());
        pref.setWeatherAlert(dto.isWeatherAlert());

        UserPreference saved = preferenceRepository.save(pref);
        return mapToDTO(saved);
    }

    private void sendEmail(NotificationRequestDTO request) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("user@example.com"); // Fetch from user-service in production
            message.setSubject("[F1 Platform] " + request.getTitle());
            message.setText(request.getMessage());
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
        }
    }

    private boolean isAlertEnabled(UserPreference pref, String type) {
        return switch (type) {
            case "RACE_START" -> pref.isRaceStartAlert();
            case "FASTEST_LAP" -> pref.isFastestLapAlert();
            case "PODIUM" -> pref.isPodiumAlert();
            case "PENALTY" -> pref.isPenaltyAlert();
            case "WEATHER" -> pref.isWeatherAlert();
            default -> true;
        };
    }

    private NotificationDTO mapToDTO(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .sessionKey(n.getSessionKey())
                .status(n.getStatus().name())
                .createdAt(n.getCreatedAt())
                .build();
    }

    private UserPreferenceDTO mapToDTO(UserPreference p) {
        return UserPreferenceDTO.builder()
                .userId(p.getUserId())
                .emailNotifications(p.isEmailNotifications())
                .pushNotifications(p.isPushNotifications())
                .raceStartAlert(p.isRaceStartAlert())
                .fastestLapAlert(p.isFastestLapAlert())
                .podiumAlert(p.isPodiumAlert())
                .penaltyAlert(p.isPenaltyAlert())
                .weatherAlert(p.isWeatherAlert())
                .build();
    }
}
