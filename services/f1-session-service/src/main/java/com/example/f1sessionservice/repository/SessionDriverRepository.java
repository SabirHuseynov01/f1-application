package com.example.f1sessionservice.repository;

import com.example.f1sessionservice.model.SessionDrivers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionDriverRepository extends JpaRepository<SessionDrivers, Long> {

    List<SessionDrivers> findBySessionId(Long sessionId);

    Optional<SessionDrivers> findBySessionIdAndDriverNumber(Long sessionId, Integer driverNumber);

    List<SessionDrivers> findByDriverCode(String driverCode);
}
