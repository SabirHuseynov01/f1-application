package com.example.f1timingservice.repository;

import com.example.f1timingservice.model.LapTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LapTimeRepository extends JpaRepository<LapTime, Long> {
    List<LapTime> findBySessionKey(Integer sessionKey);

    Page <LapTime> findBySessionKey(Integer sessionKey, Pageable pageable);

    List<LapTime> findBySessionKeyAndDriverNumber(Integer sessionKey, Integer driverNumber);

    Optional<LapTime> findBySessionKeyAndDriverNumberAndLapNumber(
            Integer sessionKey, Integer driverNumber, Integer lapNumber);

    @Query("SELECT l FROM LapTime l WHERE l.sessionKey = :sessionKey AND l.lapDuration " +
            "IS NOT NULL ORDER BY l.lapDuration ASC")
    List<LapTime> findFastestLapsBySession(Integer sessionKey);

    @Query("SELECT l FROM LapTime l WHERE l.sessionKey = :sessionKey AND l.driverNumber =" +
            " :driverNumber ORDER BY l.lapNumber ASC")
    List<LapTime> findDriverLapsOrdered(Integer sessionKey, Integer driverNumber);

    long countBySessionKey(Integer sessionKey);
}
