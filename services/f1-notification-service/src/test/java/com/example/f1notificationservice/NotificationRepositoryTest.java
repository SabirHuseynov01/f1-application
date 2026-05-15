package com.example.f1notificationservice;

import com.example.f1notificationservice.models.Notification;
import com.example.f1notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("NotificationRepository Tests")
class NotificationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepository repository;

    @Test
    @DisplayName("findByUserIdAndStatusOrderByCreatedAtDesc - returns filtered ordered results")
    void findByUserIdAndStatus_returnsOrdered() {
        LocalDateTime now = LocalDateTime.now();
        Notification n1 = Notification.builder()
                .userId(1L).type("RACE_START").title("Race").message("msg")
                .status(Notification.Status.UNREAD).createdAt(now.minusHours(2)).build();
        Notification n2 = Notification.builder()
                .userId(1L).type("FASTEST_LAP").title("Lap").message("msg")
                .status(Notification.Status.UNREAD).createdAt(now).build();
        Notification n3 = Notification.builder()
                .userId(1L).type("PODIUM").title("Podium").message("msg")
                .status(Notification.Status.READ).createdAt(now.minusHours(1)).build();

        entityManager.persist(n1);
        entityManager.persist(n2);
        entityManager.persist(n3);
        entityManager.flush();

        List<Notification> result = repository.findByUserIdAndStatusOrderByCreatedAtDesc(1L, Notification.Status.UNREAD);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getType()).isEqualTo("FASTEST_LAP");
        assertThat(result.get(1).getType()).isEqualTo("RACE_START");
    }

    @Test
    @DisplayName("findByUserIdOrderByCreatedAtDesc - returns paginated results")
    void findByUserIdOrderByCreatedAtDesc_paginated() {
        for (int i = 0; i < 15; i++) {
            entityManager.persist(Notification.builder()
                    .userId(1L).type("TYPE" + i).title("Title").message("msg")
                    .status(Notification.Status.UNREAD).createdAt(LocalDateTime.now()).build());
        }
        entityManager.flush();

        Page<Notification> page = repository.findByUserIdOrderByCreatedAtDesc(1L, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(15);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("countByUserIdAndStatus - returns correct count")
    void countByUserIdAndStatus_returnsCount() {
        entityManager.persist(Notification.builder()
                .userId(1L).type("RACE").title("T").message("m")
                .status(Notification.Status.UNREAD).createdAt(LocalDateTime.now()).build());
        entityManager.persist(Notification.builder()
                .userId(1L).type("LAP").title("T").message("m")
                .status(Notification.Status.UNREAD).createdAt(LocalDateTime.now()).build());
        entityManager.persist(Notification.builder()
                .userId(1L).type("PODIUM").title("T").message("m")
                .status(Notification.Status.READ).createdAt(LocalDateTime.now()).build());
        entityManager.flush();

        long unreadCount = repository.countByUserIdAndStatus(1L, Notification.Status.UNREAD);
        long readCount = repository.countByUserIdAndStatus(1L, Notification.Status.READ);

        assertThat(unreadCount).isEqualTo(2);
        assertThat(readCount).isEqualTo(1);
    }

    @Test
    @DisplayName("save - notification is persisted with generated id")
    void save_notificationPersistedWithId() {
        Notification notification = Notification.builder()
                .userId(1L).type("TEST").title("Title").message("msg")
                .status(Notification.Status.UNREAD).createdAt(LocalDateTime.now()).build();

        Notification saved = repository.save(notification);

        assertThat(saved.getId()).isNotNull();
    }
}
