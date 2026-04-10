package com.wft.guesscolleague.service;

import com.wft.guesscolleague.model.TelegramUser;
import com.wft.guesscolleague.repository.TelegramUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления пользователями Telegram
 * Предоставляет методы для регистрации, обновления и получения статистики пользователей
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramUserService {

    private final TelegramUserRepository userRepository;

    @Value("${admin.user.ids:}")
    private String adminUserIds;

    /**
     * Регистрация или обновление пользователя без дополнительных данных
     *
     * @param telegramId ID пользователя в Telegram
     */
    @Transactional
    public void registerOrUpdateUser(Long telegramId) {
        registerOrUpdateUser(telegramId, null, null, null);
    }

    /**
     * Регистрация или обновление пользователя с данными
     *
     * @param telegramId ID пользователя в Telegram
     * @param username   Username пользователя (без @)
     * @param firstName  Имя пользователя
     * @param lastName   Фамилия пользователя
     */
    @Transactional
    public void registerOrUpdateUser(Long telegramId, String username,
                                     String firstName, String lastName) {
        Optional<TelegramUser> existing = userRepository.findByTelegramId(telegramId);

        // Формируем полное имя из имени и фамилии
        String fullName = "";
        if (firstName != null && !firstName.isEmpty()) {
            fullName = firstName;
        }
        if (lastName != null && !lastName.isEmpty()) {
            fullName += (fullName.isEmpty() ? "" : " ") + lastName;
        }

        if (existing.isPresent()) {
            // Обновляем существующего пользователя, но только если имя не было изменено ранее
            TelegramUser user = existing.get();

            // Обновляем username только если он дефолтный
            if (username != null && !username.isEmpty() &&
                    (user.getUsername() == null || user.getUsername().startsWith("User "))) {
                user.setUsername(username);
            }

            // Обновляем полное имя только если оно дефолтное
            if (!fullName.isEmpty() &&
                    (user.getFullName() == null || user.getFullName().startsWith("User "))) {
                user.setFullName(fullName);
                user.setFirstName(firstName);
                user.setLastName(lastName);
            }

            user.setLastActive(LocalDateTime.now());
            userRepository.save(user);
            log.info("Updated existing user: {} ({})", user.getFullName(), telegramId);
        } else {
            // Создаем нового пользователя
            if (fullName.isEmpty()) {
                fullName = username != null ? username : "User " + telegramId;
            }

            TelegramUser newUser = new TelegramUser();
            newUser.setTelegramId(telegramId);
            newUser.setUsername(username);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setFullName(fullName);
            newUser.setLastActive(LocalDateTime.now());
            newUser.setTotalScore(0);
            newUser.setGamesPlayed(0);
            newUser.setCorrectAnswers(0);
            newUser.setWrongAnswers(0);
            newUser.setCurrentStreak(0);
            newUser.setBestStreak(0);
            newUser.setActive(true);
            newUser.setAdmin(isAdminUser(telegramId));  // Проверяем, является ли пользователь админом

            userRepository.save(newUser);
            log.info("Registered new user: {} ({})", newUser.getFullName(), telegramId);
        }
    }

    /**
     * Проверяет, является ли пользователь администратором
     * Администраторы задаются в application.properties через admin.user.ids
     *
     * @param telegramId ID пользователя в Telegram
     * @return true если пользователь админ, false в противном случае
     */
    protected boolean isAdminUser(Long telegramId) {
        if (adminUserIds == null || adminUserIds.isEmpty()) {
            return false;
        }
        String[] adminIds = adminUserIds.split(",");
        for (String adminId : adminIds) {
            try {
                if (Long.parseLong(adminId.trim()) == telegramId) {
                    return true;
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid admin ID format: {}", adminId);
            }
        }
        return false;
    }

    /**
     * Обновляет только общий счет пользователя
     *
     * @param telegramId ID пользователя в Telegram
     * @param score      новое значение общего счета
     */
    @Transactional
    public void updateScore(Long telegramId, int score) {
        userRepository.updateScore(telegramId, score);
        log.debug("Updated score for user {} to {}", telegramId, score);
    }

    /**
     * Обновляет полную статистику пользователя
     *
     * @param telegramId     ID пользователя в Telegram
     * @param totalScore     общее количество очков
     * @param correctAnswers количество правильных ответов
     * @param wrongAnswers   количество неправильных ответов
     * @param currentStreak  текущая серия правильных ответов
     * @param bestStreak     рекордная серия правильных ответов
     */
    @Transactional
    public void updateStats(Long telegramId, int totalScore, int correctAnswers,
                            int wrongAnswers, int currentStreak, int bestStreak) {
        userRepository.updateStats(telegramId, totalScore, correctAnswers,
                wrongAnswers, currentStreak, bestStreak);
        log.debug("Updated stats for user {}: score={}, correct={}, wrong={}",
                telegramId, totalScore, correctAnswers, wrongAnswers);
    }

    /**
     * Увеличивает счетчик сыгранных игр на 1
     * Вызывается при создании новой игровой сессии
     *
     * @param telegramId ID пользователя в Telegram
     */
    @Transactional
    public void incrementGamesPlayed(Long telegramId) {
        userRepository.incrementGamesPlayed(telegramId);
        log.info("Incremented games played for user: {}", telegramId);
    }

    /**
     * Полностью сбрасывает статистику пользователя (очки, ответы, серии)
     *
     * @param telegramId ID пользователя в Telegram
     */
    @Transactional
    public void resetUserStats(Long telegramId) {
        userRepository.updateStats(telegramId, 0, 0, 0, 0, 0);
        log.info("Reset stats for user: {}", telegramId);
    }

    /**
     * Сбрасывает только счетчик сыгранных игр
     *
     * @param telegramId ID пользователя в Telegram
     */
    @Transactional
    public void resetGamesPlayed(Long telegramId) {
        userRepository.resetGamesPlayed(telegramId);
        log.info("Reset games played for user: {}", telegramId);
    }

    /**
     * Получает пользователя по Telegram ID
     *
     * @param telegramId ID пользователя в Telegram
     * @return Optional с пользователем или пустой Optional если не найден
     */
    public Optional<TelegramUser> getUserByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }

    /**
     * Получает таблицу лидеров, отсортированную по убыванию очков
     *
     * @return список пользователей, отсортированный по totalScore DESC
     */
    public List<TelegramUser> getLeaderboard() {
        return userRepository.getLeaderboard();
    }

    /**
     * Подсчитывает количество активных пользователей за последние 24 часа
     *
     * @return количество активных пользователей
     */
    public long getActiveUsersCount() {
        LocalDateTime dayAgo = LocalDateTime.now().minusDays(1);
        return userRepository.countActiveSince(dayAgo);
    }

    /**
     * Получает всех пользователей
     *
     * @return список всех пользователей
     */
    public List<TelegramUser> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Получает статистику пользователя, выбрасывает исключение если не найден
     *
     * @param telegramId ID пользователя в Telegram
     * @return пользователь с полной статистикой
     * @throws RuntimeException если пользователь не найден
     */
    public TelegramUser getUserStats(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}