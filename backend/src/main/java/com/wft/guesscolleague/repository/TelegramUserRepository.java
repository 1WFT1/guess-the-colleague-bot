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

    @Modifying
    @Transactional
    @Query("UPDATE TelegramUser u SET u.totalScore = u.totalScore + :points, u.lastActive = CURRENT_TIMESTAMP WHERE u.telegramId = :telegramId")
    void addScore(@Param("telegramId") Long telegramId, @Param("points") int points);

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
    @Query("UPDATE TelegramUser u SET u.totalScore = 0, u.gamesPlayed = 0 WHERE u.telegramId = :telegramId")
    void resetScore(@Param("telegramId") Long telegramId);
}