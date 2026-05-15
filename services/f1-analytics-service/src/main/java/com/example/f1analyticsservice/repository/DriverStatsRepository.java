package com.example.f1analyticsservice.repository;

import com.example.f1analyticsservice.models.DriverStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverStatsRepository extends JpaRepository<DriverStats, Long> {

    List<DriverStats> findBySessionKey(Integer sessionKey);

    Optional<DriverStats> findBySessionKeyAndDriverNumber(Integer sessionKey, Integer driverNumber);
}
