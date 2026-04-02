package com.wft.guesscolleague.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

/**
 * DTO для запроса на проверку ответа
 * Отправляется с фронтенда при выборе варианта ответа
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerRequest {

    /**
     * ID игровой сессии
     * Позволяет идентифицировать текущую игру пользователя
     */
    private UUID sessionId;

    /**
     * ID вопроса, на который отвечает пользователь
     * Связывает ответ с конкретным вопросом
     */
    private UUID questionId;

    /**
     * Индекс выбранного варианта ответа
     * Значения: 0, 1, 2, 3 (соответствует позиции в массиве options)
     */
    private int selectedOptionIndex;
}