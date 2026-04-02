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

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaderboardService {

    private final WeeklyStatsRepository weeklyStatsRepository;

    public LeaderboardDTO getLeaderboard(String week, Long userId) {
        LocalDate weekStart = LocalDate.parse(week, DateTimeFormatter.ISO_WEEK_DATE);

        List<WeeklyStats> stats = weeklyStatsRepository.findByWeekStartOrderByTotalScoreDesc(weekStart);

        List<LeaderboardDTO.LeaderboardEntry> entries = stats.stream()
                .limit(100)
                .map(s -> new LeaderboardDTO.LeaderboardEntry(
                        s.getUserId(),
                        getUserFullName(s.getUserId()), // Нужно будет реализовать получение имени
                        s.getTotalScore(),
                        calculateAccuracy(s.getCorrectCount(), s.getQuestionsAnswered()),
                        s.getRank() != null ? s.getRank() : 0
                ))
                .collect(Collectors.toList());

        LeaderboardDTO.LeaderboardEntry currentUser = null;
        if (userId != null) {
            currentUser = entries.stream()
                    .filter(e -> e.getUserId().equals(userId))
                    .findFirst()
                    .orElse(null);
        }

        return new LeaderboardDTO(week, entries, currentUser);
    }

    private double calculateAccuracy(int correctCount, int totalAnswered) {
        if (totalAnswered == 0) return 0.0;
        return (double) correctCount / totalAnswered * 100;
    }

    private String getUserFullName(Long userId) {
        // Здесь нужно будет реализовать получение имени пользователя
        // Можно через Telegram API или из базы данных
        return "User " + userId;
    }
}