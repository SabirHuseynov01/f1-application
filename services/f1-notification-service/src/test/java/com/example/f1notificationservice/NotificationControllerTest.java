package com.example.f1notificationservice;

import com.example.f1notificationservice.controller.NotificationController;
import com.example.f1notificationservice.dto.NotificationDTO;
import com.example.f1notificationservice.dto.NotificationRequestDTO;
import com.example.f1notificationservice.dto.UserPreferenceDTO;
import com.example.f1notificationservice.models.Notification;
import com.example.f1notificationservice.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController Tests")
class NotificationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("GET /{userId} - returns unread notifications")
    void getUnread_returnsUnreadNotifications() throws Exception {
        List<NotificationDTO> notifications = List.of(
                NotificationDTO.builder()
                        .id(1L).type("RACE_START").title("Race Started")
                        .message("Bahrain GP has started!").sessionKey(9158)
                        .status("UNREAD").createdAt(LocalDateTime.now()).build(),
                NotificationDTO.builder()
                        .id(2L).type("FASTEST_LAP").title("Fastest Lap")
                        .message("Verstappen set fastest lap: 1:32.456").sessionKey(9158)
                        .status("UNREAD").createdAt(LocalDateTime.now()).build()
        );

        when(notificationService.getUserNotifications(1L, Notification.Status.UNREAD))
                .thenReturn(notifications);

        mockMvc.perform(get("/api/notifications/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].type", is("RACE_START")))
                .andExpect(jsonPath("$[0].title", is("Race Started")))
                .andExpect(jsonPath("$[1].type", is("FASTEST_LAP")));
    }

    @Test
    @DisplayName("GET /{userId} - with status parameter filters correctly")
    void getUnread_withStatusParameter_filters() throws Exception {
        List<NotificationDTO> readNotifications = List.of(
                NotificationDTO.builder()
                        .id(1L).type("PODIUM").title("Podium Ceremony")
                        .message("Verstappen wins!").status("READ").build()
        );

        when(notificationService.getUserNotifications(1L, Notification.Status.READ))
                .thenReturn(readNotifications);

        mockMvc.perform(get("/api/notifications/{userId}", 1L)
                        .param("status", "READ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status", is("READ")));
    }

    @Test
    @DisplayName("GET /{userId}/all - returns paginated notifications")
    void getAll_returnsPaginated() throws Exception {
        Page<NotificationDTO> page = new PageImpl<>(List.of(
                NotificationDTO.builder().id(1L).type("RACE_START").title("Race").build()
        ), PageRequest.of(0, 10), 1);

        when(notificationService.getUserNotificationsPaginated(eq(1L), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/notifications/{userId}/all", 1L)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("POST /{notificationId}/read - marks notification as read")
    void markRead_marksAsRead() throws Exception {
        doNothing().when(notificationService).markAsRead(1L);

        mockMvc.perform(post("/api/notifications/{notificationId}/read", 1L))
                .andExpect(status().isOk());

        verify(notificationService).markAsRead(1L);
    }

    @Test
    @DisplayName("PUT /preferences/{userId} - updates preferences")
    void updatePreferences_updatesSuccessfully() throws Exception {
        UserPreferenceDTO dto = UserPreferenceDTO.builder()
                .userId(1L)
                .emailNotifications(true)
                .pushNotifications(true)
                .raceStartAlert(true)
                .fastestLapAlert(false)
                .podiumAlert(true)
                .penaltyAlert(true)
                .weatherAlert(false)
                .build();

        when(notificationService.updatePreferences(1L, dto)).thenReturn(dto);

        mockMvc.perform(put("/api/notifications/preferences/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.emailNotifications", is(true)))
                .andExpect(jsonPath("$.fastestLapAlert", is(false)));
    }

    @Test
    @DisplayName("POST /broadcast - broadcasts notification")
    void broadcast_sendsNotification() throws Exception {
        NotificationRequestDTO request = new NotificationRequestDTO();
        request.setType("RACE_START");
        request.setTitle("Race Started");
        request.setMessage("The race has begun!");
        request.setSessionKey(9158);
        request.setUserId(null);

        doNothing().when(notificationService).broadcastNotification(any());

        mockMvc.perform(post("/api/notifications/broadcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(notificationService).broadcastNotification(any());
    }

    @Test
    @DisplayName("GET /health - returns UP")
    void healthCheck_returnsUp() throws Exception {
        mockMvc.perform(get("/api/notifications/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("notification-service UP"));
    }
}
