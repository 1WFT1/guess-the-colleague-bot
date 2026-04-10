package com.wft.guesscolleague.repository;

import com.wft.guesscolleague.model.QuestionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с попытками ответов (таблица question_attempts)
 */
public interface QuestionAttemptRepository extends JpaRepository<QuestionAttempt, UUID> {

    /**
     * Находит все попытки ответов для сессии
     * @param sessionId ID сессии
     * @return список попыток
     */
    List<QuestionAttempt> findBySessionId(UUID sessionId);

    /**
     * Находит попытки пользователя за период
     * @param userId ID пользователя
     * @param startDate начало периода
     * @param endDate конец периода
     * @return список попыток
     */
    @Query("SELECT qa FROM QuestionAttempt qa WHERE qa.session.userId = :userId AND qa.createdAt BETWEEN :startDate AND :endDate")
    List<QuestionAttempt> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Подсчитывает количество правильных ответов пользователя
     * @param userId ID пользователя
     * @return количество правильных ответов
     */
    @Query("SELECT COUNT(qa) FROM QuestionAttempt qa WHERE qa.session.userId = :userId AND qa.isCorrect = true")
    long countCorrectAnswersByUserId(@Param("userId") Long userId);

    /**
     * Подсчитывает общее количество ответов пользователя
     * @param userId ID пользователя
     * @return общее количество ответов
     */
    @Query("SELECT COUNT(qa) FROM QuestionAttempt qa WHERE qa.session.userId = :userId")
    long countTotalAnswersByUserId(@Param("userId") Long userId);

    /**
     * Находит все попытки сессии, отсортированные по времени (новые сверху)
     * @param sessionId ID сессии
     * @return список попыток
     */
    @Query("SELECT qa FROM QuestionAttempt qa WHERE qa.session.id = :sessionId ORDER BY qa.createdAt DESC")
    List<QuestionAttempt> findBySessionIdOrderByCreatedAtDesc(@Param("sessionId") UUID sessionId);

    @Modifying
    @Query("DELETE FROM QuestionAttempt q WHERE q.createdAt < :date")
    int deleteOldAttempts(@Param("date") LocalDateTime date);
}