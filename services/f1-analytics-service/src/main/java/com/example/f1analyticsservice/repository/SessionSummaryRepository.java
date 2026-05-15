package com.example.f1analyticsservice.repository;

import com.example.f1analyticsservice.models.SessionSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionSummaryRepository extends JpaRepository<SessionSummary, Long> {

    Optional<SessionSummary> findBySessionKey(Integer sessionKey);
}
