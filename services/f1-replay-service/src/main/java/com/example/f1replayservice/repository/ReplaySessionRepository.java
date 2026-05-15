package com.example.f1replayservice.repository;

import com.example.f1replayservice.model.ReplaySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReplaySessionRepository extends JpaRepository<ReplaySession, Long> {

    List<ReplaySession> findByStatus(ReplaySession.Status status);

    Optional<ReplaySession> findByOriginalSessionKey(Integer sessionKey);
}
