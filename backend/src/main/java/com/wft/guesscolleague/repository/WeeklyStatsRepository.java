package com.wft.guesscolleague.repository;

import com.wft.guesscolleague.model.WeeklyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с еженедельной статистикой (таблица weekly_stats)
 */
public interface WeeklyStatsRepository extends JpaRepository<WeeklyStats, UUID> {

    /**
     * Находит статистику за неделю, отсортированную по очкам (от большего к меньшему)
     * @param weekStart дата начала недели
     * @return список статистики (лидерборд)
     */
    List<WeeklyStats> findByWeekStartOrderByTotalScoreDesc(LocalDate weekStart);

    /**
     * Находит статистику пользователя за конкретную неделю
     * @param userId ID пользователя
     * @param weekStart дата начала недели
     * @return статистика пользователя
     */
    Optional<WeeklyStats> findByUserIdAndWeekStart(Long userId, LocalDate weekStart);

    /**
     * Получает лидерборд за неделю
     * @param weekStart дата начала недели
     * @return список игроков, отсортированный по очкам
     */
    @Query("SELECT ws FROM WeeklyStats ws WHERE ws.weekStart = :weekStart ORDER BY ws.totalScore DESC")
    List<WeeklyStats> getLeaderboardByWeek(@Param("weekStart") LocalDate weekStart);

    /**
     * Получает историю статистики пользователя
     * @param userId ID пользователя
     * @param startDate начиная с этой даты
     * @return список недельной статистики
     */
    @Query("SELECT ws FROM WeeklyStats ws WHERE ws.userId = :userId AND ws.weekStart >= :startDate ORDER BY ws.weekStart DESC")
    List<WeeklyStats> getUserHistory(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);

    /**
     * Получает лидерборд за период
     * @param startDate начало периода
     * @param endDate конец периода
     * @return список игроков, отсортированный по очкам
     */
    @Query("SELECT ws FROM WeeklyStats ws WHERE ws.weekStart BETWEEN :startDate AND :endDate ORDER BY ws.totalScore DESC")
    List<WeeklyStats> getLeaderboardForDateRange(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);
}