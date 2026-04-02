package com.wft.guesscolleague.service;

import com.wft.guesscolleague.dto.LeaderboardDTO;
import com.wft.guesscolleague.model.WeeklyStats;
import com.wft.guesscolleague.repository.WeeklyStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для работы с таблицей лидеров (рейтингом игроков)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeaderboardService {

    private final WeeklyStatsRepository weeklyStatsRepository;

    /**
     * Получает таблицу лидеров за указанную неделю
     *
     * @param week неделя в формате ISO (например, "2026-W12")
     * @param userId ID пользователя (опционально, для получения его позиции)
     * @return DTO с топ-100 игроков и позицией текущего пользователя
     */
    public LeaderboardDTO getLeaderboard(String week, Long userId) {
        // Парсим строку недели в дату (например, "2026-W12" → 2026-03-24)
        LocalDate weekStart = LocalDate.parse(week, DateTimeFormatter.ISO_WEEK_DATE);
        log.info("Getting leaderboard for week starting: {}", weekStart);

        // Получаем статистику за неделю, отсортированную по очкам (от большего к меньшему)
        List<WeeklyStats> stats = weeklyStatsRepository.findByWeekStartOrderByTotalScoreDesc(weekStart);
        log.info("Found {} players for this week", stats.size());

        // Преобразуем в DTO (берем только топ-100)
        List<LeaderboardDTO.LeaderboardEntry> entries = stats.stream()
                .limit(100)  // Ограничиваем 100 записями
                .map(s -> new LeaderboardDTO.LeaderboardEntry(
                        s.getUserId(),
                        getUserFullName(s.getUserId()),  // Получаем имя пользователя
                        s.getTotalScore(),
                        calculateAccuracy(s.getCorrectCount(), s.getQuestionsAnswered()),
                        s.getRank() != null ? s.getRank() : 0  // Ранг, если есть
                ))
                .collect(Collectors.toList());

        // Находим позицию текущего пользователя в рейтинге
        LeaderboardDTO.LeaderboardEntry currentUser = null;
        if (userId != null) {
            currentUser = entries.stream()
                    .filter(e -> e.getUserId().equals(userId))
                    .findFirst()
                    .orElse(null);

            if (currentUser != null) {
                log.info("Current user {} found at rank: {}", userId, currentUser.getRank());
            } else {
                log.info("Current user {} not found in top 100", userId);
            }
        }

        return new LeaderboardDTO(week, entries, currentUser);
    }

    /**
     * Рассчитывает точность ответов в процентах
     * @param correctCount количество правильных ответов
     * @param totalAnswered общее количество отвеченных вопросов
     * @return процент правильных ответов
     */
    private double calculateAccuracy(int correctCount, int totalAnswered) {
        if (totalAnswered == 0) return 0.0;
        return (double) correctCount / totalAnswered * 100;
    }

    /**
     * Получает полное имя пользователя по его ID
     * TODO: Реализовать получение имени через Telegram API или из БД
     *
     * @param userId ID пользователя в Telegram
     * @return имя пользователя
     */
    private String getUserFullName(Long userId) {
        // Здесь нужно будет реализовать получение имени пользователя
        // Варианты:
        // 1. Через Telegram Bot API (getChat)
        // 2. Из отдельной таблицы users в БД
        // 3. Из кэша
        return "User " + userId;  // Временно, пока не реализовано
    }
}