package com.wft.guesscolleague.model;

import com.wft.guesscolleague.config.JsonbType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Модель попытки ответа на вопрос
 * Сохраняет каждый ответ пользователя на вопрос
 */
@Entity  // Указывает, что это сущность JPA для сохранения в БД
@Table(name = "question_attempts")  // Имя таблицы в базе данных
@Data  // Lombok: генерирует геттеры, сеттеры, toString, equals, hashCode
@NoArgsConstructor  // Lombok: генерирует конструктор без параметров
public class QuestionAttempt {

    /**
     * Уникальный идентификатор попытки ответа
     * Автоматически генерируется как UUID
     */
    @Id  // Первичный ключ
    @GeneratedValue(strategy = GenerationType.UUID)  // Автоматическая генерация UUID
    private UUID id;

    /**
     * Ссылка на игровую сессию
     * Многие попытки могут принадлежать одной сессии
     * Загружается лениво (только когда обращаются к этому полю)
     */
    @ManyToOne(fetch = FetchType.LAZY)  // Связь многие-к-одному, ленивая загрузка
    @JoinColumn(name = "session_id", nullable = false)  // Внешний ключ на таблицу game_sessions
    private GameSession session;

    /**
     * Ссылка на сотрудника (правильный ответ на вопрос)
     * Многие попытки могут ссылаться на одного сотрудника
     * Загружается лениво (только когда обращаются к этому полю)
     */
    @ManyToOne(fetch = FetchType.LAZY)  // Связь многие-к-одному, ленивая загрузка
    @JoinColumn(name = "employee_id", nullable = false)  // Внешний ключ на таблицу employees
    private Employee employee;

    /**
     * Индекс выбранного пользователем варианта ответа
     * Значения: 0, 1, 2, 3 (соответствует позиции в массиве options)
     * Может быть null, если ответ еще не выбран
     */
    @Column(name = "selected_option")
    private Integer selectedOption;

    /**
     * Флаг правильности ответа
     * true - ответ правильный
     * false - ответ неправильный
     * null - ответ еще не дан
     */
    @Column(name = "is_correct")
    private Boolean isCorrect;

    /**
     * Количество очков, начисленных/списанных за ответ
     * Положительное число (+5) - за правильный ответ
     * Отрицательное число (-6) - за неправильный ответ
     */
    @Column(name = "points_delta")
    private Integer pointsDelta;

    /**
     * JSON-строка с вариантами ответов
     * Хранит структуру:
     * [
     *   {"employeeId": "uuid", "fullName": "Анна Иванова"},
     *   {"employeeId": "uuid", "fullName": "Петр Сидоров"},
     *   ...
     * ]
     * Используется для восстановления вопроса при проверке ответа
     */
    @Column(name = "options", columnDefinition = "jsonb")  // Тип колонки - JSONB в PostgreSQL
    @Type(JsonbType.class)  // Кастомный тип для преобразования JSONB
    private String options;

    /**
     * Дата и время создания записи
     * Автоматически заполняется при создании, нельзя изменить
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Определение типа вопроса
     */
    @Column(name = "question_type")
    private String questionType;
}