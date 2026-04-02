package com.wft.guesscolleague.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Модель еженедельной статистики игрока
 * Хранит итоги игры за каждую неделю
 */
@Entity
@Table(name = "weekly_stats")
@Data
@NoArgsConstructor
public class WeeklyStats {

    /**
     * Уникальный идентификатор записи статистики
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * ID пользователя в Telegram
     * Связь с конкретным игроком
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Дата начала недели (обычно понедельник)
     * Например: 2026-03-24
     */
    @Column(name = "week_start")
    private LocalDate weekStart;

    /**
     * Дата окончания недели (обычно воскресенье)
     * Например: 2026-03-30
     */
    @Column(name = "week_end")
    private LocalDate weekEnd;

    /**
     * Общее количество очков, набранных за неделю
     */
    @Column(name = "total_score")
    private int totalScore;

    /**
     * Количество отвеченных вопросов за неделю
     */
    @Column(name = "questions_answered")
    private int questionsAnswered;

    /**
     * Количество правильных ответов за неделю
     */
    @Column(name = "correct_count")
    private int correctCount;

    /**
     * Количество неправильных ответов за неделю
     */
    @Column(name = "wrong_count")
    private int wrongCount;

    /**
     * Место в рейтинге за эту неделю (1, 2, 3...)
     * Вычисляется после подведения итогов
     */
    private Integer rank;

    /**
     * Дата и время создания записи
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}