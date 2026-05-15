package com.example.f1telemetryservice.repository;


import com.example.f1telemetryservice.model.PositionData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface PositionDataRepository extends JpaRepository<PositionData, Long> {

    List<PositionData> findBySessionKeyAndDriverNumber(Integer sessionKey, Integer driverNumber);

    Optional<PositionData> findTopBySessionKeyAndDriverNumberOrderByTimestampDesc(
            Integer sessionKey, Integer driverNumber);

}
