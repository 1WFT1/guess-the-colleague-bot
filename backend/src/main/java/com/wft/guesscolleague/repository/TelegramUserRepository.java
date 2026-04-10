package com.wft.guesscolleague.repository;

import com.wft.guesscolleague.model.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TelegramUserRepository extends JpaRepository<TelegramUser, UUID> {

    Optional<TelegramUser> findByTelegramId(Long telegramId);

    Optional<TelegramUser> findByUsername(String username);

    List<TelegramUser> findByIsActiveTrue();

    List<TelegramUser> findByIsAdminTrue();

    // Метод обновления счета
    @Modifying
    @Transactional
    @Query("UPDATE TelegramUser u SET u.totalScore = :score, u.lastActive = CURRENT_TIMESTAMP WHERE u.telegramId = :telegramId")
    void updateScore(@Param("telegramId") Long telegramId, @Param("score") int score);

    // Метод обновления полной статистики
    @Modifying
    @Transactional
    @Query("UPDATE TelegramUser u SET " +
            "u.totalScore = :totalScore, " +
            "u.correctAnswers = :correctAnswers, " +
            "u.wrongAnswers = :wrongAnswers, " +
            "u.currentStreak = :currentStreak, " +
            "u.bestStreak = :bestStreak, " +
            "u.lastActive = CURRENT_TIMESTAMP " +
            "WHERE u.telegramId = :telegramId")
    void updateStats(@Param("telegramId") Long telegramId,
                     @Param("totalScore") int totalScore,
                     @Param("correctAnswers") int correctAnswers,
                     @Param("wrongAnswers") int wrongAnswers,
                     @Param("currentStreak") int currentStreak,
                     @Param("bestStreak") int bestStreak);

    @Modifying
    @Transactional
    @Query("UPDATE TelegramUser u SET u.gamesPlayed = u.gamesPlayed + 1 WHERE u.telegramId = :telegramId")
    void incrementGamesPlayed(@Param("telegramId") Long telegramId);

    @Query("SELECT u FROM TelegramUser u ORDER BY u.totalScore DESC")
    List<TelegramUser> getLeaderboard();

    @Query("SELECT COUNT(u) FROM TelegramUser u WHERE u.lastActive > :since")
    long countActiveSince(@Param("since") LocalDateTime since);

    @Modifying
    @Transactional
    @Query("UPDATE TelegramUser u SET u.gamesPlayed = 0 WHERE u.telegramId = :telegramId")
    void resetGamesPlayed(@Param("telegramId") Long telegramId);
}