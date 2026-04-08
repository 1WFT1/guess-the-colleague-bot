package com.wft.guesscolleague.controller;

import com.wft.guesscolleague.dto.UserStatsDTO;
import com.wft.guesscolleague.model.TelegramUser;
import com.wft.guesscolleague.service.TelegramUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final TelegramUserService telegramUserService;

    @GetMapping("/stats")
    public ResponseEntity<UserStatsDTO> getUserStats(@RequestParam Long userId) {
        log.info("Getting stats for user: {}", userId);
        TelegramUser user = telegramUserService.getUserStats(userId);
        return ResponseEntity.ok(new UserStatsDTO(user));
    }

    @PostMapping("/update-stats")
    public ResponseEntity<UserStatsDTO> updateStats(
            @RequestParam Long userId,
            @RequestParam int totalScore,
            @RequestParam int correctAnswers,
            @RequestParam int wrongAnswers,
            @RequestParam int currentStreak,
            @RequestParam int bestStreak) {

        log.info("Updating stats for user: {} (score: {})", userId, totalScore);

        // Вызываем метод updateStats (он возвращает void)
        telegramUserService.updateStats(userId, totalScore, correctAnswers,
                wrongAnswers, currentStreak, bestStreak);

        // После обновления получаем актуальные данные пользователя
        TelegramUser user = telegramUserService.getUserStats(userId);

        return ResponseEntity.ok(new UserStatsDTO(user));
    }
}