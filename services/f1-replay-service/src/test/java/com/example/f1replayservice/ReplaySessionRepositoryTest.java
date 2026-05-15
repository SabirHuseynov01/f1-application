package com.example.f1replayservice;

import com.example.f1replayservice.model.ReplaySession;
import com.example.f1replayservice.repository.ReplaySessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("ReplaySessionRepository Tests")
class ReplaySessionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReplaySessionRepository repository;

    @Test
    @DisplayName("findByStatus - returns sessions by status")
    void findByStatus_returnsMatching() {
        ReplaySession s1 = ReplaySession.builder()
                .originalSessionKey(9158).replayName("R1")
                .status(ReplaySession.Status.PLAYING).createdAt(LocalDateTime.now()).build();
        ReplaySession s2 = ReplaySession.builder()
                .originalSessionKey(9523).replayName("R2")
                .status(ReplaySession.Status.PAUSED).createdAt(LocalDateTime.now()).build();
        ReplaySession s3 = ReplaySession.builder()
                .originalSessionKey(9999).replayName("R3")
                .status(ReplaySession.Status.PLAYING).createdAt(LocalDateTime.now()).build();

        entityManager.persist(s1);
        entityManager.persist(s2);
        entityManager.persist(s3);
        entityManager.flush();

        List<ReplaySession> result = repository.findByStatus(ReplaySession.Status.PLAYING);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ReplaySession::getReplayName).containsExactlyInAnyOrder("R1", "R3");
    }

    @Test
    @DisplayName("findByOriginalSessionKey - returns session by key")
    void findByOriginalSessionKey_returnsSession() {
        ReplaySession session = ReplaySession.builder()
                .originalSessionKey(9158).replayName("Bahrain GP")
                .status(ReplaySession.Status.CREATED).createdAt(LocalDateTime.now()).build();

        entityManager.persist(session);
        entityManager.flush();

        Optional<ReplaySession> result = repository.findByOriginalSessionKey(9158);

        assertThat(result).isPresent();
        assertThat(result.get().getReplayName()).isEqualTo("Bahrain GP");
    }

    @Test
    @DisplayName("findByOriginalSessionKey - non-existing returns empty")
    void findByOriginalSessionKey_nonExisting_returnsEmpty() {
        Optional<ReplaySession> result = repository.findByOriginalSessionKey(999);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("save - session persisted with generated id")
    void save_sessionPersistedWithId() {
        ReplaySession session = ReplaySession.builder()
                .originalSessionKey(9158).replayName("Test")
                .status(ReplaySession.Status.CREATED).createdAt(LocalDateTime.now()).build();

        ReplaySession saved = repository.save(session);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("findByStatus - empty result for non-matching status")
    void findByStatus_noMatch_returnsEmpty() {
        ReplaySession session = ReplaySession.builder()
                .originalSessionKey(9158).replayName("Test")
                .status(ReplaySession.Status.COMPLETED).createdAt(LocalDateTime.now()).build();
        entityManager.persist(session);
        entityManager.flush();

        List<ReplaySession> result = repository.findByStatus(ReplaySession.Status.PLAYING);

        assertThat(result).isEmpty();
    }
}
