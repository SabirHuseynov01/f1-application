package com.example.f1sessionservice.repository;

import com.example.f1sessionservice.model.Circuit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CircuitRepository extends JpaRepository<Circuit, Long> {

    Optional<Circuit> findByCircuitKey(Integer circuitKey);
    boolean existsByCircuitKey(Integer circuitKey);
}
