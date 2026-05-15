package com.example.f1timingservice.repository;

import com.example.f1timingservice.model.Stint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StintRepository extends JpaRepository<Stint, Long> {
    List<Stint> findBySessionKey(Integer sessionKey);

    List<Stint> findBySessionKeyAndDriverNumber(Integer sessionKey, Integer driverNumber);

    Optional<Stint> findBySessionKeyAndDriverNumberAndStintNumber(Integer sessionKey, Integer driverNumber,
                                                                  Integer stintNumber);

    long countBySessionKey(Integer sessionKey);
}
