package com.wft.guesscolleague.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO для передачи вопроса на фронтенд
 * Содержит все данные, необходимые для отображения вопроса
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {

    /**
     * Уникальный ID вопроса
     * Используется при отправке ответа для связи с конкретным вопросом
     */
    private UUID questionId;

    /**
     * URL ссылка на фото сотрудника
     * Отображается в верхней части карточки вопроса
     */
    private String photoUrl;

    /**
     * Список вариантов ответов (ФИО сотрудников)
     * Всегда 4 варианта: 1 правильный + 3 случайных дистрактора
     * Перемешаны в случайном порядке
     */
    private List<String> options;
}