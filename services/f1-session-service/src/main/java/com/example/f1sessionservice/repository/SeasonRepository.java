package com.example.f1sessionservice.repository;

import com.example.f1sessionservice.model.Season;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeasonRepository extends JpaRepository<Season, Long> {

    Optional<Season> findByYear(Integer year);
    boolean existsByYear(Integer year);
}
