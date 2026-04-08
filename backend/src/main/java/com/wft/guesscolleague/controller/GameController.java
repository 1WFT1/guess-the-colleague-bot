package com.wft.guesscolleague.controller;

import com.wft.guesscolleague.dto.AnswerRequest;
import com.wft.guesscolleague.dto.AnswerResponse;
import com.wft.guesscolleague.dto.QuestionDTO;
import com.wft.guesscolleague.service.GameService;
import com.wft.guesscolleague.service.TelegramUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class GameController {

    private final GameService gameService;
    private final TelegramUserService telegramUserService;

    @PostMapping("/session")
    public ResponseEntity<UUID> createSession(
            @RequestParam Long userId,
            @RequestParam(required = false) Long chatId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName) {

        log.info("Creating session for user: {} (username: {}, firstName: {}, lastName: {})",
                userId, username, firstName, lastName);

        // Регистрируем пользователя с полученными данными
        telegramUserService.registerOrUpdateUser(userId, username, firstName, lastName);

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
            @RequestBody AnswerRequest request) {
        AnswerResponse response = gameService.processAnswer(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-stats")
    public ResponseEntity<?> resetStats(@RequestParam Long userId) {
        log.info("Resetting stats for user: {}", userId);
        telegramUserService.resetUserStats(userId);
        return ResponseEntity.ok().build();
    }
}