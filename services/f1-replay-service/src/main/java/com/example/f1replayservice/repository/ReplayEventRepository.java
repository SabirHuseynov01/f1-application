package com.example.f1replayservice.repository;

import com.example.f1replayservice.model.ReplayEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReplayEventRepository extends JpaRepository<ReplayEvent, Long> {

    List<ReplayEvent> findByReplaySessionIdAndEventTimeOffsetBetween(
            Long replaySessionId, Long startOffset, Long endOffset);

    Page<ReplayEvent> findByReplaySessionIdOrderByEventTimeOffsetAsc(
            Long replaySessionId, Pageable pageable);

    List<ReplayEvent> findByReplaySessionIdAndEventTypeOrderByEventTimeOffsetAsc(
            Long replaySessionId, String eventType);
}
