package com.wft.guesscolleague.repository;

import com.wft.guesscolleague.model.WeeklyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WeeklyStatsRepository extends JpaRepository<WeeklyStats, UUID> {

    List<WeeklyStats> findByWeekStartOrderByTotalScoreDesc(LocalDate weekStart);

    Optional<WeeklyStats> findByUserIdAndWeekStart(Long userId, LocalDate weekStart);

    @Query("SELECT ws FROM WeeklyStats ws WHERE ws.weekStart = :weekStart ORDER BY ws.totalScore DESC")
    List<WeeklyStats> getLeaderboardByWeek(@Param("weekStart") LocalDate weekStart);

    @Query("SELECT ws FROM WeeklyStats ws WHERE ws.userId = :userId AND ws.weekStart >= :startDate ORDER BY ws.weekStart DESC")
    List<WeeklyStats> getUserHistory(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);

    @Query("SELECT ws FROM WeeklyStats ws WHERE ws.weekStart BETWEEN :startDate AND :endDate ORDER BY ws.totalScore DESC")
    List<WeeklyStats> getLeaderboardForDateRange(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);
}