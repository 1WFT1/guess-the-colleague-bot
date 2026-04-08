package com.wft.guesscolleague.service;

import com.wft.guesscolleague.dto.UserStatsDTO;
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

    @Transactional
    public TelegramUser registerOrUpdateUser(Long telegramId, String username,
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
            newUser.setCorrectAnswers(0);
            newUser.setWrongAnswers(0);
            newUser.setCurrentStreak(0);
            newUser.setBestStreak(0);
            newUser.setActive(true);
            newUser.setAdmin(isAdminUser(telegramId));

            log.info("Registered new user: {} ({})", newUser.getFullName(), telegramId);
            return userRepository.save(newUser);
        }
    }

    @Transactional
    public void updateStats(Long telegramId, int totalScore, int correctAnswers,
                            int wrongAnswers, int currentStreak, int bestStreak) {
        userRepository.updateStats(telegramId, totalScore, correctAnswers,
                wrongAnswers, currentStreak, bestStreak);
        log.debug("Updated stats for user {}: score={}, correct={}, wrong={}",
                telegramId, totalScore, correctAnswers, wrongAnswers);
    }

    @Transactional
    public void incrementGamesPlayed(Long telegramId) {
        userRepository.incrementGamesPlayed(telegramId);
    }

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