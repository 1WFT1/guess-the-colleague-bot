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

/**
 * Репозиторий для работы с игровыми сессиями (таблица game_sessions)
 */
public interface GameSessionRepository extends JpaRepository<GameSession, UUID> {

    /**
     * Находит активную сессию пользователя
     * @param userId ID пользователя в Telegram
     * @return активная сессия, если есть
     */
    Optional<GameSession> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * Находит все сессии пользователя, отсортированные по дате начала
     * @param userId ID пользователя
     * @return список сессий (от новых к старым)
     */
    List<GameSession> findByUserIdOrderByStartedAtDesc(Long userId);

    /**
     * Находит активные сессии за период
     * @param startDate начало периода
     * @param endDate конец периода
     * @return список активных сессий
     */
    @Query("SELECT gs FROM GameSession gs WHERE gs.startedAt BETWEEN :startDate AND :endDate AND gs.isActive = true")
    List<GameSession> findActiveSessionsBetweenDates(@Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Архивирует старые сессии (устанавливает isActive = false)
     * @param date сессии старше этой даты будут архивированы
     */
    @Modifying
    @Transactional
    @Query("UPDATE GameSession gs SET gs.isActive = false WHERE gs.startedAt < :date")
    void archiveOldSessions(@Param("date") LocalDateTime date);
}