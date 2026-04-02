package com.wft.guesscolleague.controller;

import com.wft.guesscolleague.dto.AnswerRequest;
import com.wft.guesscolleague.dto.AnswerResponse;
import com.wft.guesscolleague.dto.QuestionDTO;
import com.wft.guesscolleague.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Основной игровой контроллер
 * Обрабатывает все игровые действия пользователя
 * Доступен по адресу: /api/game
 */
@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
@Tag(name = "Game Controller", description = "API для управления игровым процессом")
public class GameController {

    private final GameService gameService;

    /**
     * Создает новую игровую сессию
     * POST /api/game/session?userId=123&chatId=123
     *
     * @param userId ID пользователя в Telegram
     * @param chatId ID чата в Telegram
     * @return UUID созданной сессии
     */
    @Operation(summary = "Создать игровую сессию",
            description = "Создает новую игровую сессию для пользователя или возвращает существующую активную")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Сессия успешно создана",
                    content = @Content(schema = @Schema(implementation = UUID.class))),
            @ApiResponse(responseCode = "400", description = "Неверные параметры запроса"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/session")
    public ResponseEntity<UUID> createSession(
            @Parameter(description = "ID пользователя Telegram", required = true)
            @RequestParam Long userId,
            @Parameter(description = "ID чата Telegram")
            @RequestParam(required = false) Long chatId) {
        UUID sessionId = gameService.createSession(userId, chatId).getId();
        return ResponseEntity.ok(sessionId);
    }

    /**
     * Генерирует следующий вопрос для игровой сессии
     * GET /api/game/next-question?sessionId=xxx
     *
     * @param sessionId ID активной игровой сессии
     * @return вопрос с фото и вариантами ответов
     */
    @Operation(summary = "Получить следующий вопрос",
            description = "Генерирует и возвращает следующий вопрос для игровой сессии")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Вопрос успешно сгенерирован",
                    content = @Content(schema = @Schema(implementation = QuestionDTO.class))),
            @ApiResponse(responseCode = "400", description = "Сессия не найдена или неактивна"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/next-question")
    public ResponseEntity<QuestionDTO> getNextQuestion(
            @Parameter(description = "ID игровой сессии", required = true)
            @RequestParam UUID sessionId) {
        QuestionDTO question = gameService.generateNextQuestion(sessionId);
        return ResponseEntity.ok(question);
    }

    /**
     * Обрабатывает ответ пользователя на вопрос
     * POST /api/game/answer
     *
     * @param request данные ответа (sessionId, questionId, selectedOptionIndex)
     * @return результат проверки ответа (правильно/неправильно, начисленные очки)
     */
    @Operation(summary = "Отправить ответ на вопрос",
            description = "Проверяет ответ пользователя и начисляет баллы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ответ обработан",
                    content = @Content(schema = @Schema(implementation = AnswerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/answer")
    public ResponseEntity<AnswerResponse> submitAnswer(
            @Parameter(description = "Данные ответа", required = true)
            @RequestBody AnswerRequest request) {
        AnswerResponse response = gameService.processAnswer(request);
        return ResponseEntity.ok(response);
    }
}