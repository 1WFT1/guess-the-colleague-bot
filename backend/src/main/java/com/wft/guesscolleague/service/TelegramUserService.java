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

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramUserService {

    private final TelegramUserRepository userRepository;

    @Value("${admin.user.ids:}")
    private String adminUserIds;

    // Регистрация пользователя без дополнительных данных
    @Transactional
    public void registerOrUpdateUser(Long telegramId) {
        registerOrUpdateUser(telegramId, null, null, null);
    }

    // Регистрация пользователя с данными (возвращает void, а не TelegramUser)
    @Transactional
    public void registerOrUpdateUser(Long telegramId, String username,
                                     String firstName, String lastName) {
        Optional<TelegramUser> existing = userRepository.findByTelegramId(telegramId);

        String fullName = "";
        if (firstName != null && !firstName.isEmpty()) {
            fullName = firstName;
        }
        if (lastName != null && !lastName.isEmpty()) {
            fullName += (fullName.isEmpty() ? "" : " ") + lastName;
        }
        if (fullName.isEmpty()) {
            fullName = username != null ? username : "User " + telegramId;
        }

        if (existing.isPresent()) {
            TelegramUser user = existing.get();
            if (username != null) user.setUsername(username);
            if (firstName != null) user.setFirstName(firstName);
            if (lastName != null) user.setLastName(lastName);
            user.setFullName(fullName);
            user.setLastActive(LocalDateTime.now());
            userRepository.save(user);
            log.info("Updated existing user: {} ({})", user.getFullName(), telegramId);
        } else {
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
            newUser.setAdmin(isAdminUser(telegramId));

            userRepository.save(newUser);
            log.info("Registered new user: {} ({})", newUser.getFullName(), telegramId);
        }
    }

    // Метод проверки админа (сделайте public или protected)
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

    // Метод обновления счета (updateScore)
    @Transactional
    public void updateScore(Long telegramId, int score) {
        userRepository.updateScore(telegramId, score);
        log.debug("Updated score for user {} to {}", telegramId, score);
    }

    // Метод обновления полной статистики
    @Transactional
    public void updateStats(Long telegramId, int totalScore, int correctAnswers,
                            int wrongAnswers, int currentStreak, int bestStreak) {
        userRepository.updateStats(telegramId, totalScore, correctAnswers,
                wrongAnswers, currentStreak, bestStreak);
        log.debug("Updated stats for user {}: score={}, correct={}, wrong={}",
                telegramId, totalScore, correctAnswers, wrongAnswers);
    }

    // Метод увеличения счетчика игр
    @Transactional
    public void incrementGamesPlayed(Long telegramId) {
        userRepository.incrementGamesPlayed(telegramId);
    }

    // Метод сброса статистики
    @Transactional
    public void resetUserStats(Long telegramId) {
        userRepository.updateStats(telegramId, 0, 0, 0, 0, 0);
        log.info("Reset stats for user: {}", telegramId);
    }

    // Получение пользователя
    public Optional<TelegramUser> getUserByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }

    public List<TelegramUser> getLeaderboard() {
        return userRepository.getLeaderboard();
    }

    public long getActiveUsersCount() {
        LocalDateTime dayAgo = LocalDateTime.now().minusDays(1);
        return userRepository.countActiveSince(dayAgo);
    }

    public List<TelegramUser> getAllUsers() {
        return userRepository.findAll();
    }

    public TelegramUser getUserStats(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}