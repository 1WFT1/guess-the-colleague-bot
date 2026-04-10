package com.wft.guesscolleague.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Модель игровой сессии пользователя
 * Хранит состояние игры для конкретного пользователя
 */
@Entity  // Указывает, что это сущность JPA для сохранения в БД
@Table(name = "game_sessions")  // Имя таблицы в базе данных
@Data  // Lombok: генерирует геттеры, сеттеры, toString, equals, hashCode
@NoArgsConstructor  // Lombok: генерирует конструктор без параметров
public class GameSession {

    /**
     * Уникальный идентификатор игровой сессии
     * Автоматически генерируется как UUID
     */
    @Id  // Первичный ключ
    @GeneratedValue(strategy = GenerationType.UUID)  // Автоматическая генерация UUID
    private UUID id;

    /**
     * ID пользователя в Telegram
     * Уникальный идентификатор, который Telegram присваивает каждому пользователю
     * Обязательное поле, не может быть null
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * ID чата в Telegram
     * Может быть ID личного чата или группового чата
     * Необязательное поле
     */
    @Column(name = "chat_id")
    private Long chatId;

    /**
     * Общее количество набранных очков
     * Увеличивается на +5 за правильный ответ
     * Уменьшается на -6 за неправильный ответ
     * Не может быть меньше 0
     */
    @Column(name = "total_score")
    private int totalScore = 0;

    /**
     * Количество правильных ответов
     * Увеличивается при каждом правильном ответе
     */
    @Column(name = "correct_answers")
    private int correctAnswers = 0;

    /**
     * Количество неправильных ответов
     * Увеличивается при каждом неправильном ответе
     */
    @Column(name = "wrong_answers")
    private int wrongAnswers = 0;

    /**
     * Статус активности сессии
     * true - сессия активна, пользователь может играть
     * false - сессия завершена или архивна
     */
    @Column(name = "is_active")
    private boolean isActive = true;

    /**
     * Дата и время начала сессии
     * Автоматически заполняется при создании, нельзя изменить
     */
    @CreationTimestamp  // Автоматически заполняется Hibernate
    @Column(name = "started_at", updatable = false)  // Нельзя обновить после создания
    private LocalDateTime startedAt;

    /**
     * Дата и время последней активности
     * Обновляется при каждом действии пользователя (ответе на вопрос)
     */
    @UpdateTimestamp  // Автоматически обновляется Hibernate
    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    /**
     * Добавляет очки к общему счету
     * @param points количество очков для добавления (может быть отрицательным)
     *               Очки не могут уйти ниже 0
     */
    public void addScore(int points) {
        this.totalScore = Math.max(0, this.totalScore + points);
    }

    /**
     * Увеличивает счетчик правильных ответов на 1
     * Вызывается при правильном ответе пользователя
     */
    public void incrementCorrect() {
        this.correctAnswers++;
    }

    /**
     * Увеличивает счетчик неправильных ответов на 1
     * Вызывается при неправильном ответе пользователя
     */
    public void incrementWrong() {
        this.wrongAnswers++;
    }

    @Column(name = "current_streak")
    private int currentStreak = 0;

    @Column(name = "best_streak")
    private int bestStreak = 0;

    @Column(name = "current_question_type")
    private String currentQuestionType;

    @Column(name = "game_mode")
    private String gameMode = "name";
}