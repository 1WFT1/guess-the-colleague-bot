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

    /**
     * Регистрация или обновление пользователя (без дополнительных данных)
     */
    @Transactional
    public TelegramUser registerOrUpdateUser(Long telegramId) {
        return registerOrUpdateUser(telegramId, null, null, null);
    }

    /**
     * Регистрация или обновление пользователя с данными
     */
    @Transactional
    public TelegramUser registerOrUpdateUser(Long telegramId, String username,
                                             String firstName, String lastName) {
        Optional<TelegramUser> existing = userRepository.findByTelegramId(telegramId);

        // Формируем полное имя из переданных данных
        String fullName = "";
        if (firstName != null && !firstName.isEmpty()) {
            fullName = firstName;
        }
        if (lastName != null && !lastName.isEmpty()) {
            fullName += (fullName.isEmpty() ? "" : " ") + lastName;
        }
        if (fullName.isEmpty() && username != null && !username.isEmpty()) {
            fullName = username;
        }
        if (fullName.isEmpty()) {
            fullName = "User " + telegramId;
        }

        if (existing.isPresent()) {
            TelegramUser user = existing.get();
            if (username != null) user.setUsername(username);
            if (firstName != null) user.setFirstName(firstName);
            if (lastName != null) user.setLastName(lastName);
            user.setFullName(fullName);  // Обновляем имя
            user.setLastActive(LocalDateTime.now());
            log.info("Updated existing user: {} ({})", user.getFullName(), telegramId);
            return userRepository.save(user);
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
            newUser.setActive(true);
            newUser.setAdmin(isAdminUser(telegramId));

            log.info("Registered new user: {} ({})", newUser.getFullName(), telegramId);
            return userRepository.save(newUser);
        }
    }

    private boolean isAdminUser(Long telegramId) {
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

    public Optional<TelegramUser> getUserByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }

    @Transactional
    public void addScore(Long telegramId, int points) {
        userRepository.addScore(telegramId, points);
        log.debug("Added {} points to user {}, new total should be updated", points, telegramId);
    }

    @Transactional
    public void incrementGamesPlayed(Long telegramId) {
        userRepository.incrementGamesPlayed(telegramId);
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

    @Transactional
    public void resetUserStats(Long telegramId) {
        // Обнуляем счет пользователя
        userRepository.resetScore(telegramId);
        log.info("Reset stats for user: {}", telegramId);
    }

}