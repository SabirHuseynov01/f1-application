package com.example.f1telemetryservice;

import com.example.f1telemetryservice.model.CarTelemetry;
import com.example.f1telemetryservice.repository.CarTelemetryRepository;
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
@DisplayName("CarTelemetryRepository Tests")
class CarTelemetryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CarTelemetryRepository repository;

    @Test
    @DisplayName("findBySessionKeyAndDriverNumber - returns matching telemetry")
    void findBySessionKeyAndDriverNumber_returnsMatching() {
        CarTelemetry t1 = CarTelemetry.builder()
                .sessionKey(9158).driverNumber(1).speed(280.0).timestamp(LocalDateTime.now()).build();
        CarTelemetry t2 = CarTelemetry.builder()
                .sessionKey(9158).driverNumber(1).speed(285.0).timestamp(LocalDateTime.now()).build();
        CarTelemetry t3 = CarTelemetry.builder()
                .sessionKey(9158).driverNumber(44).speed(290.0).timestamp(LocalDateTime.now()).build();

        entityManager.persist(t1);
        entityManager.persist(t2);
        entityManager.persist(t3);
        entityManager.flush();

        List<CarTelemetry> result = repository.findBySessionKeyAndDriverNumber(9158, 1);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CarTelemetry::getSpeed).containsExactlyInAnyOrder(280.0, 285.0);
    }

    @Test
    @DisplayName("findTopBySessionKeyAndDriverNumberOrderByTimestampDesc - returns latest")
    void findTopByOrderByTimestampDesc_returnsLatest() {
        LocalDateTime now = LocalDateTime.now();
        CarTelemetry old = CarTelemetry.builder()
                .sessionKey(9158).driverNumber(1).speed(280.0).timestamp(now.minusMinutes(5)).build();
        CarTelemetry latest = CarTelemetry.builder()
                .sessionKey(9158).driverNumber(1).speed(290.0).timestamp(now).build();

        entityManager.persist(old);
        entityManager.persist(latest);
        entityManager.flush();

        Optional<CarTelemetry> result = repository.findTopBySessionKeyAndDriverNumberOrderByTimestampDesc(9158, 1);

        assertThat(result).isPresent();
        assertThat(result.get().getSpeed()).isEqualTo(290.0);
    }

    @Test
    @DisplayName("findBySessionKeyAndDriverNumberAndTimestampBetween - returns events in range")
    void findByTimestampBetween_returnsInRange() {
        LocalDateTime now = LocalDateTime.now();
        CarTelemetry t1 = CarTelemetry.builder()
                .sessionKey(9158).driverNumber(1).speed(280.0).timestamp(now.minusMinutes(10)).build();
        CarTelemetry t2 = CarTelemetry.builder()
                .sessionKey(9158).driverNumber(1).speed(285.0).timestamp(now.minusMinutes(5)).build();
        CarTelemetry t3 = CarTelemetry.builder()
                .sessionKey(9158).driverNumber(1).speed(290.0).timestamp(now).build();

        entityManager.persist(t1);
        entityManager.persist(t2);
        entityManager.persist(t3);
        entityManager.flush();

        List<CarTelemetry> result = repository.findBySessionKeyAndDriverNumberAndTimestampBetween(
                9158, 1, now.minusMinutes(7), now.minusMinutes(2));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSpeed()).isEqualTo(285.0);
    }

    @Test
    @DisplayName("findLatestBySession - returns latest for each driver")
    void findLatestBySession_returnsLatestPerDriver() {
        LocalDateTime now = LocalDateTime.now();
        CarTelemetry t1 = CarTelemetry.builder()
                .sessionKey(9158).driverNumber(1).speed(280.0).timestamp(now.minusMinutes(2)).build();
        CarTelemetry t2 = CarTelemetry.builder()
                .sessionKey(9158).driverNumber(1).speed(285.0).timestamp(now).build();
        CarTelemetry t3 = CarTelemetry.builder()
                .sessionKey(9158).driverNumber(44).speed(290.0).timestamp(now.minusMinutes(1)).build();

        entityManager.persist(t1);
        entityManager.persist(t2);
        entityManager.persist(t3);
        entityManager.flush();

        List<CarTelemetry> result = repository.findLatestBySession(9158);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("save - telemetry is persisted with generated id")
    void save_telemetryPersistedWithId() {
        CarTelemetry telemetry = CarTelemetry.builder()
                .sessionKey(9158).driverNumber(1).speed(280.0).timestamp(LocalDateTime.now()).build();

        CarTelemetry saved = repository.save(telemetry);

        assertThat(saved.getId()).isNotNull();
    }
}
