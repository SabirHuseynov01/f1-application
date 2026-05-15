package com.example.f1notificationservice;

import com.example.f1notificationservice.dto.NotificationDTO;
import com.example.f1notificationservice.dto.NotificationRequestDTO;
import com.example.f1notificationservice.dto.UserPreferenceDTO;
import com.example.f1notificationservice.models.Notification;
import com.example.f1notificationservice.models.UserPreference;
import com.example.f1notificationservice.repository.NotificationRepository;
import com.example.f1notificationservice.repository.UserPreferenceRepository;
import com.example.f1notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Tests")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserPreferenceRepository preferenceRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationRequestDTO requestDTO;
    private UserPreference userPreference;

    @BeforeEach
    void setUp() {
        requestDTO = new NotificationRequestDTO();
        requestDTO.setType("RACE_START");
        requestDTO.setTitle("Race Started");
        requestDTO.setMessage("Bahrain GP has started!");
        requestDTO.setSessionKey(9158);
        requestDTO.setUserId(1L);

        userPreference = UserPreference.builder()
                .id(1L)
                .userId(1L)
                .emailNotifications(true)
                .pushNotifications(true)
                .raceStartAlert(true)
                .fastestLapAlert(true)
                .podiumAlert(true)
                .penaltyAlert(true)
                .weatherAlert(false)
                .build();
    }

    @Test
    @DisplayName("broadcastNotification - saves notification and sends WebSocket")
    void broadcastNotification_savesAndSends() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(1L);
            return n;
        });
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(userPreference));

        notificationService.broadcastNotification(requestDTO);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo("RACE_START");
        assertThat(saved.getTitle()).isEqualTo("Race Started");
        assertThat(saved.getStatus()).isEqualTo(Notification.Status.UNREAD);

        verify(messagingTemplate).convertAndSend(eq("/topic/notifications"), any(NotificationDTO.class));
    }

    @Test
    @DisplayName("broadcastNotification - sends email when enabled")
    void broadcastNotification_emailEnabled_sendsEmail() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(userPreference));

        notificationService.broadcastNotification(requestDTO);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("broadcastNotification - does not send email when disabled")
    void broadcastNotification_emailDisabled_noEmail() {
        userPreference.setEmailNotifications(false);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(userPreference));

        notificationService.broadcastNotification(requestDTO);

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("broadcastNotification - does not send email for disabled alert type")
    void broadcastNotification_alertDisabled_noEmail() {
        requestDTO.setType("WEATHER");
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(userPreference));

        notificationService.broadcastNotification(requestDTO);

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("broadcastNotification - no userId does not send email")
    void broadcastNotification_noUserId_noEmail() {
        requestDTO.setUserId(null);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.broadcastNotification(requestDTO);

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("sendUrgentNotification - sends to specific user WebSocket")
    void sendUrgentNotification_sendsToUser() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.sendUrgentNotification(requestDTO);

        verify(messagingTemplate).convertAndSend(eq("/topic/notifications/1"), any(NotificationDTO.class));
    }

    @Test
    @DisplayName("getUserNotifications - filters by status")
    void getUserNotifications_filtersByStatus() {
        Notification notif = Notification.builder()
                .id(1L).userId(1L).type("RACE_START").title("Race").message("Started")
                .status(Notification.Status.UNREAD).createdAt(LocalDateTime.now()).build();

        when(notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(1L, Notification.Status.UNREAD))
                .thenReturn(List.of(notif));

        List<NotificationDTO> result = notificationService.getUserNotifications(1L, Notification.Status.UNREAD);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("UNREAD");
    }

    @Test
    @DisplayName("markAsRead - updates status to READ")
    void markAsRead_updatesStatus() {
        Notification notif = Notification.builder()
                .id(1L).status(Notification.Status.UNREAD).build();

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notif));

        notificationService.markAsRead(1L);

        assertThat(notif.getStatus()).isEqualTo(Notification.Status.READ);
        assertThat(notif.getReadAt()).isNotNull(); // hatali
    }

    @Test
    @DisplayName("updatePreferences - creates new preferences if not exists")
    void updatePreferences_newPreferences_creates() {
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(preferenceRepository.save(any(UserPreference.class))).thenAnswer(inv -> inv.getArgument(0));

        UserPreferenceDTO dto = UserPreferenceDTO.builder()
                .userId(1L).emailNotifications(false).pushNotifications(true)
                .raceStartAlert(true).fastestLapAlert(false).build();

        UserPreferenceDTO result = notificationService.updatePreferences(1L, dto);

        assertThat(result.isEmailNotifications()).isFalse();
        assertThat(result.isPushNotifications()).isTrue();
        verify(preferenceRepository).save(any(UserPreference.class));
    }

    @Test
    @DisplayName("updatePreferences - updates existing preferences")
    void updatePreferences_existing_updates() {
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(userPreference));
        when(preferenceRepository.save(any(UserPreference.class))).thenAnswer(inv -> inv.getArgument(0));

        UserPreferenceDTO dto = UserPreferenceDTO.builder()
                .userId(1L).emailNotifications(false).weatherAlert(true).build();

        UserPreferenceDTO result = notificationService.updatePreferences(1L, dto);

        assertThat(result.isEmailNotifications()).isFalse();
        assertThat(result.isWeatherAlert()).isTrue();
    }

    @Test
    @DisplayName("getUserNotificationsPaginated - returns paginated results")
    void getUserNotificationsPaginated_returnsPage() {
        Page<Notification> page = new PageImpl<>(List.of(
                Notification.builder().id(1L).type("RACE_START").build()
        ), PageRequest.of(0, 10), 1);

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any()))
                .thenReturn(page);

        Page<NotificationDTO> result = notificationService.getUserNotificationsPaginated(1L, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getType()).isEqualTo("RACE_START");
    }
}
