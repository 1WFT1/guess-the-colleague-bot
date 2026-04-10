package com.wft.guesscolleague.controller;

import com.wft.guesscolleague.dto.UserStatsDTO;
import com.wft.guesscolleague.model.TelegramUser;
import com.wft.guesscolleague.service.TelegramUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Контроллер для управления пользователями и их статистикой
 * Доступен по адресу: /api/user
 *
 * Предоставляет API для получения и обновления статистики игроков,
 * а также для работы с таблицей лидеров
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final TelegramUserService telegramUserService;

    /**
     * Получить статистику конкретного пользователя
     * GET /api/user/stats?userId=xxx
     *
     * @param userId ID пользователя в Telegram
     * @return DTO с полной статистикой пользователя (очки, ответы, серии)
     */
    @GetMapping("/stats")
    public ResponseEntity<UserStatsDTO> getUserStats(@RequestParam Long userId) {
        log.info("Getting stats for user: {}", userId);
        TelegramUser user = telegramUserService.getUserStats(userId);
        return ResponseEntity.ok(new UserStatsDTO(user));
    }

    /**
     * Обновить статистику пользователя после игры
     * POST /api/user/update-stats
     *
     * @param userId         ID пользователя в Telegram
     * @param totalScore     Общее количество очков
     * @param correctAnswers Количество правильных ответов
     * @param wrongAnswers   Количество неправильных ответов
     * @param currentStreak  Текущая серия правильных ответов
     * @param bestStreak     Рекордная серия правильных ответов
     * @return Обновленный DTO со статистикой пользователя
     */
    @PostMapping("/update-stats")
    public ResponseEntity<UserStatsDTO> updateStats(
            @RequestParam Long userId,
            @RequestParam int totalScore,
            @RequestParam int correctAnswers,
            @RequestParam int wrongAnswers,
            @RequestParam int currentStreak,
            @RequestParam int bestStreak) {

        log.info("Updating stats for user: {} (score: {})", userId, totalScore);

        // Обновляем статистику пользователя
        telegramUserService.updateStats(userId, totalScore, correctAnswers,
                wrongAnswers, currentStreak, bestStreak);

        // Получаем обновленные данные пользователя
        TelegramUser user = telegramUserService.getUserStats(userId);

        return ResponseEntity.ok(new UserStatsDTO(user));
    }

    /**
     * Получить всех пользователей (для таблицы лидеров)
     * GET /api/user/all
     *
     * @return список всех пользователей с их статистикой
     */
    @GetMapping("/all")
    public ResponseEntity<List<UserStatsDTO>> getAllUsers() {
        log.info("Getting all users");
        List<TelegramUser> users = telegramUserService.getAllUsers();
        List<UserStatsDTO> dtos = users.stream()
                .map(UserStatsDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Сбросить счетчик игр пользователя
     * POST /api/user/reset-games-played?userId=xxx
     *
     * Используется при сбросе статистики в админ-панели
     *
     * @param userId ID пользователя в Telegram
     * @return пустой ответ с кодом 200 при успешном сбросе
     */
    @PostMapping("/reset-games-played")
    public ResponseEntity<?> resetGamesPlayed(@RequestParam Long userId) {
        log.info("Resetting games played for user: {}", userId);
        telegramUserService.resetGamesPlayed(userId);
        return ResponseEntity.ok().build();
    }
}