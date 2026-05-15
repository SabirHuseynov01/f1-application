package com.example.f1replayservice;

import com.example.f1replayservice.model.ReplayEvent;
import com.example.f1replayservice.model.ReplaySession;
import com.example.f1replayservice.repository.ReplayEventRepository;
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
@DisplayName("ReplayEventRepository Tests")
class ReplayEventRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReplayEventRepository repository;

    private ReplaySession session;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        session = ReplaySession.builder()
                .originalSessionKey(9158).replayName("Test")
                .status(ReplaySession.Status.CREATED).createdAt(LocalDateTime.now()).build();
        entityManager.persist(session);
    }

    @Test
    @DisplayName("findByReplaySessionIdAndEventTimeOffsetBetween - returns events in range")
    void findByReplaySessionIdAndEventTimeOffsetBetween_returnsInRange() {
        ReplayEvent e1 = ReplayEvent.builder()
                .replaySession(session).eventType("LAP_TIME").eventTimeOffset(50000L)
                .driverNumber(1).payload("{}").build();
        ReplayEvent e2 = ReplayEvent.builder()
                .replaySession(session).eventType("POSITION").eventTimeOffset(60000L)
                .driverNumber(1).payload("{}").build();
        ReplayEvent e3 = ReplayEvent.builder()
                .replaySession(session).eventType("TELEMETRY").eventTimeOffset(70000L)
                .driverNumber(1).payload("{}").build();

        entityManager.persist(e1);
        entityManager.persist(e2);
        entityManager.persist(e3);
        entityManager.flush();

        List<ReplayEvent> result = repository.findByReplaySessionIdAndEventTimeOffsetBetween(
                session.getId(), 55000L, 65000L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEventType()).isEqualTo("POSITION");
    }

    @Test
    @DisplayName("findByReplaySessionIdOrderByEventTimeOffsetAsc - returns paginated ordered events")
    void findByReplaySessionIdOrderByEventTimeOffsetAsc_paginated() {
        for (int i = 0; i < 15; i++) {
            entityManager.persist(ReplayEvent.builder()
                    .replaySession(session).eventType("TYPE" + i)
                    .eventTimeOffset((long) i * 1000).driverNumber(1).payload("{}").build());
        }
        entityManager.flush();

        Page<ReplayEvent> page = repository.findByReplaySessionIdOrderByEventTimeOffsetAsc(
                session.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(15);
        assertThat(page.getContent().get(0).getEventTimeOffset()).isEqualTo(0L);
        assertThat(page.getContent().get(1).getEventTimeOffset()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("findByReplaySessionIdAndEventTypeOrderByEventTimeOffsetAsc - filters by type")
    void findByReplaySessionIdAndEventType_filtersByType() {
        ReplayEvent e1 = ReplayEvent.builder()
                .replaySession(session).eventType("LAP_TIME").eventTimeOffset(1000L)
                .driverNumber(1).payload("{}").build();
        ReplayEvent e2 = ReplayEvent.builder()
                .replaySession(session).eventType("POSITION").eventTimeOffset(2000L)
                .driverNumber(1).payload("{}").build();
        ReplayEvent e3 = ReplayEvent.builder()
                .replaySession(session).eventType("LAP_TIME").eventTimeOffset(3000L)
                .driverNumber(1).payload("{}").build();

        entityManager.persist(e1);
        entityManager.persist(e2);
        entityManager.persist(e3);
        entityManager.flush();

        List<ReplayEvent> result = repository.findByReplaySessionIdAndEventTypeOrderByEventTimeOffsetAsc(
                session.getId(), "LAP_TIME");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ReplayEvent::getEventType).containsOnly("LAP_TIME");
    }

    @Test
    @DisplayName("save - event persisted with generated id")
    void save_eventPersistedWithId() {
        ReplayEvent event = ReplayEvent.builder()
                .replaySession(session).eventType("TEST").eventTimeOffset(0L)
                .driverNumber(1).payload("{}").build();

        ReplayEvent saved = repository.save(event);

        assertThat(saved.getId()).isNotNull();
    }
}
