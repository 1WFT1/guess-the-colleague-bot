package com.wft.guesscolleague.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для ответа на запрос проверки ответа
 * Отправляется с бэкенда после обработки ответа пользователя
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResponse {

    /**
     * Флаг правильности ответа
     * true - ответ правильный
     * false - ответ неправильный
     */
    private boolean isCorrect;

    /**
     * Количество начисленных/списанных очков
     * +5 за правильный ответ
     * -6 за неправильный ответ
     */
    private int pointsDelta;

    /**
     * Общее количество очков после ответа
     * Актуальный счет пользователя
     */
    private int newTotalScore;

    /**
     * Правильный ответ (ФИО сотрудника)
     * Отображается при ошибке
     */
    private String correctAnswer;

    /**
     * Текстовое сообщение для пользователя
     * Например: "Верно! +5 баллов" или "Ошибка! -6 баллов"
     */
    private String message;
}