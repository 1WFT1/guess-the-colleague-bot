package com.wft.guesscolleague.controller;

import com.wft.guesscolleague.dto.LeaderboardDTO;
import com.wft.guesscolleague.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для работы с таблицей лидеров
 * Доступен по адресу: /api/leaderboard
 */
@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    /**
     * Получает таблицу лидеров за указанную неделю
     * GET /api/leaderboard?week=2026-W12&userId=123
     *
     * @param week неделя в формате ISO (например: 2026-W12)
     * @param userId ID пользователя (опционально, для получения его позиции)
     * @return таблица лидеров с топ-100 и позицией текущего пользователя
     */
    @GetMapping
    public ResponseEntity<LeaderboardDTO> getLeaderboard(
            @RequestParam String week,
            @RequestParam(required = false) Long userId) {
        LeaderboardDTO leaderboard = leaderboardService.getLeaderboard(week, userId);
        return ResponseEntity.ok(leaderboard);
    }
}