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

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Game Controller", description = "API для управления игровым процессом")
public class GameController {

    private final GameService gameService;

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