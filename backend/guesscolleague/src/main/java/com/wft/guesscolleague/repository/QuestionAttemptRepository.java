package com.wft.guesscolleague.repository;

import com.wft.guesscolleague.model.QuestionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface QuestionAttemptRepository extends JpaRepository<QuestionAttempt, UUID> {

    List<QuestionAttempt> findBySessionId(UUID sessionId);

    @Query("SELECT qa FROM QuestionAttempt qa WHERE qa.session.userId = :userId AND qa.createdAt BETWEEN :startDate AND :endDate")
    List<QuestionAttempt> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(qa) FROM QuestionAttempt qa WHERE qa.session.userId = :userId AND qa.isCorrect = true")
    long countCorrectAnswersByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(qa) FROM QuestionAttempt qa WHERE qa.session.userId = :userId")
    long countTotalAnswersByUserId(@Param("userId") Long userId);

    @Query("SELECT qa FROM QuestionAttempt qa WHERE qa.session.id = :sessionId ORDER BY qa.createdAt DESC")
    List<QuestionAttempt> findBySessionIdOrderByCreatedAtDesc(@Param("sessionId") UUID sessionId);
}