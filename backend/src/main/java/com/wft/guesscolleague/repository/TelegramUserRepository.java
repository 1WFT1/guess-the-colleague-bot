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

/**
 * Репозиторий для работы с пользователями Telegram
 * Предоставляет методы для доступа и модификации данных в таблице telegram_users
 */
public interface TelegramUserRepository extends JpaRepository<TelegramUser, UUID> {

    /**
     * Найти пользователя по его Telegram ID
     * @param telegramId уникальный идентификатор пользователя в Telegram
     * @return Optional с пользователем или пустой Optional если не найден
     */
    Optional<TelegramUser> findByTelegramId(Long telegramId);

    /**
     * Найти пользователя по его username в Telegram
     * @param username имя пользователя в Telegram (без @)
     * @return Optional с пользователем или пустой Optional если не найден
     */
    Optional<TelegramUser> findByUsername(String username);

    /**
     * Получить всех активных пользователей
     * @return список пользователей с isActive = true
     */
    List<TelegramUser> findByIsActiveTrue();

    /**
     * Получить всех администраторов
     * @return список пользователей с isAdmin = true
     */
    List<TelegramUser> findByIsAdminTrue();

    /**
     * Обновить только общий счет пользователя
     * Используется для быстрого обновления очков без изменения остальной статистики
     *
     * @param telegramId ID пользователя в Telegram
     * @param score новое значение общего счета
     */
    @Modifying
    @Transactional
    @Query("UPDATE TelegramUser u SET u.totalScore = :score, u.lastActive = CURRENT_TIMESTAMP WHERE u.telegramId = :telegramId")
    void updateScore(@Param("telegramId") Long telegramId, @Param("score") int score);

    /**
     * Обновить полную статистику пользователя
     * Вызывается после каждого ответа на вопрос
     *
     * @param telegramId ID пользователя в Telegram
     * @param totalScore общее количество очков
     * @param correctAnswers количество правильных ответов
     * @param wrongAnswers количество неправильных ответов
     * @param currentStreak текущая серия правильных ответов
     * @param bestStreak рекордная серия правильных ответов
     */
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

    /**
     * Увеличить счетчик сыгранных игр на 1
     * Вызывается при создании новой игровой сессии
     *
     * @param telegramId ID пользователя в Telegram
     */
    @Modifying
    @Transactional
    @Query("UPDATE TelegramUser u SET u.gamesPlayed = u.gamesPlayed + 1 WHERE u.telegramId = :telegramId")
    void incrementGamesPlayed(@Param("telegramId") Long telegramId);

    /**
     * Получить таблицу лидеров, отсортированную по убыванию очков
     * @return список пользователей, отсортированный по totalScore DESC
     */
    @Query("SELECT u FROM TelegramUser u ORDER BY u.totalScore DESC")
    List<TelegramUser> getLeaderboard();

    /**
     * Подсчитать количество активных пользователей за последние 24 часа
     * Используется для статистики "Активных сегодня"
     *
     * @param since временная метка (обычно 24 часа назад от текущего момента)
     * @return количество пользователей, активных после указанного времени
     */
    @Query("SELECT COUNT(u) FROM TelegramUser u WHERE u.lastActive > :since")
    long countActiveSince(@Param("since") LocalDateTime since);

    /**
     * Сбросить счетчик сыгранных игр до 0
     * Используется при сбросе статистики пользователя
     *
     * @param telegramId ID пользователя в Telegram
     */
    @Modifying
    @Transactional
    @Query("UPDATE TelegramUser u SET u.gamesPlayed = 0 WHERE u.telegramId = :telegramId")
    void resetGamesPlayed(@Param("telegramId") Long telegramId);
}