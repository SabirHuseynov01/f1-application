package com.example.f1sessionservice.repository;


import com.example.f1sessionservice.model.Session;
import com.example.f1sessionservice.model.SessionType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

    @EntityGraph(attributePaths = {"season", "circuit"})
    Optional<Session> findBySessionKey(Integer sessionKey);

    @EntityGraph(attributePaths = {"season", "circuit"})
    List<Session> findBySeasonId(Long seasonId);

    @EntityGraph(attributePaths = {"season", "circuit"})
    List<Session> findByCircuitId(Long circuitId);


    List<Session> findBySessionType(SessionType sessionType);


    @EntityGraph(attributePaths = {"season", "circuit"})
    @Query("select s from Session s where s.season.year = :year")
    List<Session> findBySeasonYear(Integer year);

    @EntityGraph(attributePaths = {"season", "circuit"})
    @Query("select s from Session s where s.season.year = :year and s.sessionType = :type")
    List<Session> findBySeasonYearAndType(Integer year, SessionType type);
}
