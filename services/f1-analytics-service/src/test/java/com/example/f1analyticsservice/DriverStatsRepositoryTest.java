package com.example.f1analyticsservice;

import com.example.f1analyticsservice.models.DriverStats;
import com.example.f1analyticsservice.repository.DriverStatsRepository;
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
@DisplayName("DriverStatsRepository Tests")
class DriverStatsRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DriverStatsRepository repository;

    @Test
    @DisplayName("findBySessionKey - returns all driver stats for session")
    void findBySessionKey_returnsAllDrivers() {
        DriverStats s1 = DriverStats.builder()
                .sessionKey(9158).driverNumber(1).totalLaps(57)
                .fastestLapTime(92.456).avgLapTime(95.0)
                .maxSpeed(325.0).calculatedAt(LocalDateTime.now()).build();
        DriverStats s2 = DriverStats.builder()
                .sessionKey(9158).driverNumber(44).totalLaps(57)
                .fastestLapTime(91.5).avgLapTime(94.5)
                .maxSpeed(328.0).calculatedAt(LocalDateTime.now()).build();
        DriverStats s3 = DriverStats.builder()
                .sessionKey(9523).driverNumber(1).totalLaps(70)
                .fastestLapTime(95.0).avgLapTime(97.0)
                .maxSpeed(320.0).calculatedAt(LocalDateTime.now()).build();

        entityManager.persist(s1);
        entityManager.persist(s2);
        entityManager.persist(s3);
        entityManager.flush();

        List<DriverStats> result = repository.findBySessionKey(9158);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(DriverStats::getDriverNumber).containsExactlyInAnyOrder(1, 44);
    }

    @Test
    @DisplayName("findBySessionKeyAndDriverNumber - returns specific driver")
    void findBySessionKeyAndDriverNumber_returnsSpecific() {
        DriverStats stats = DriverStats.builder()
                .sessionKey(9158).driverNumber(1).totalLaps(57)
                .fastestLapTime(92.456).avgLapTime(95.0)
                .maxSpeed(325.0).calculatedAt(LocalDateTime.now()).build();

        entityManager.persist(stats);
        entityManager.flush();

        Optional<DriverStats> result = repository.findBySessionKeyAndDriverNumber(9158, 1);

        assertThat(result).isPresent();
        assertThat(result.get().getFastestLapTime()).isEqualTo(92.456);
    }

    @Test
    @DisplayName("findBySessionKeyAndDriverNumber - non-existing returns empty")
    void findBySessionKeyAndDriverNumber_nonExisting_returnsEmpty() {
        Optional<DriverStats> result = repository.findBySessionKeyAndDriverNumber(999, 999);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("save - stats persisted with generated id")
    void save_statsPersistedWithId() {
        DriverStats stats = DriverStats.builder()
                .sessionKey(9158).driverNumber(1).totalLaps(57)
                .fastestLapTime(92.456).avgLapTime(95.0)
                .calculatedAt(LocalDateTime.now()).build();

        DriverStats saved = repository.save(stats);

        assertThat(saved.getId()).isNotNull();
    }
}
