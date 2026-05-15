package com.example.f1telemetryservice.repository;


import com.example.f1telemetryservice.model.CarTelemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CarTelemetryRepository extends JpaRepository<CarTelemetry, Long> {
    List<CarTelemetry> findBySessionKeyAndDriverNumber(Integer sessionKey, Integer driverNumber);

    List<CarTelemetry> findBySessionKeyAndDriverNumberAndTimestampBetween(
            Integer sessionKey, Integer driverNumber, LocalDateTime start, LocalDateTime end);

    @Query("SELECT c FROM CarTelemetry c WHERE c.sessionKey = :sessionKey " +
            "AND c.timestamp = (SELECT MAX(c2.timestamp) FROM CarTelemetry c2 " +
            "WHERE c2.sessionKey = :sessionKey AND c2.driverNumber = c.driverNumber)")
    List<CarTelemetry> findLatestBySession(Integer sessionKey);

    Optional<CarTelemetry> findTopBySessionKeyAndDriverNumberOrderByTimestampDesc(
            Integer sessionKey, Integer driverNumber);
}
