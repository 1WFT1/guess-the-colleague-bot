package com.wft.guesscolleague.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для передачи данных таблицы лидеров
 * Используется для отображения рейтинга игроков
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardDTO {

    /**
     * Период (неделя) для которого составлен рейтинг
     * Например: "2026-W12" (12-я неделя 2026 года)
     */
    private String week;

    /**
     * Список записей лидерборда (топ-100)
     * Отсортирован по убыванию очков
     */
    private List<LeaderboardEntry> entries;

    /**
     * Данные текущего пользователя
     * Для отображения его позиции в рейтинге
     */
    private LeaderboardEntry currentUser;

    /**
     * Внутренний класс для записи в лидерборде
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaderboardEntry {

        /**
         * ID пользователя в Telegram
         */
        private Long userId;

        /**
         * Полное имя пользователя
         */
        private String fullName;

        /**
         * Общее количество набранных очков
         */
        private int totalScore;

        /**
         * Точность ответов в процентах
         * Рассчитывается: (correct / total) * 100
         */
        private double accuracy;

        /**
         * Место в рейтинге (1, 2, 3...)
         */
        private int rank;
    }
}