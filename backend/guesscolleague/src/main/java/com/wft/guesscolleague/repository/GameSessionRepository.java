package com.wft.guesscolleague.repository;

import com.wft.guesscolleague.model.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameSessionRepository extends JpaRepository<GameSession, UUID> {

    Optional<GameSession> findByUserIdAndIsActiveTrue(Long userId);

    List<GameSession> findByUserIdOrderByStartedAtDesc(Long userId);

    @Query("SELECT gs FROM GameSession gs WHERE gs.startedAt BETWEEN :startDate AND :endDate AND gs.isActive = true")
    List<GameSession> findActiveSessionsBetweenDates(@Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    @Modifying
    @Transactional
    @Query("UPDATE GameSession gs SET gs.isActive = false WHERE gs.startedAt < :date")
    void archiveOldSessions(@Param("date") LocalDateTime date);
}