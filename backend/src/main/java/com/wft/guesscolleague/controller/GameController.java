package com.wft.guesscolleague.controller;

import com.wft.guesscolleague.dto.AnswerRequest;
import com.wft.guesscolleague.dto.AnswerResponse;
import com.wft.guesscolleague.dto.QuestionDTO;
import com.wft.guesscolleague.dto.UserStatsDTO;
import com.wft.guesscolleague.model.TelegramUser;
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

    /**
     * Создает новую игровую сессию
     * POST /api/game/session
     *
     * @param userId    ID пользователя в Telegram
     * @param chatId    ID чата (опционально)
     * @param username  Имя пользователя Telegram (опционально)
     * @param firstName Имя (опционально)
     * @param lastName  Фамилия (опционально)
     * @param gameMode  Режим игры: "name" - угадать сотрудника, "department" - угадать отдел
     * @return UUID созданной сессии
     */
    @PostMapping("/session")
    public ResponseEntity<UUID> createSession(
            @RequestParam Long userId,
            @RequestParam(required = false) Long chatId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false, defaultValue = "name") String gameMode) {

        log.info("Creating session for user: {} with gameMode: {}", userId, gameMode);

        // Регистрируем или обновляем пользователя в БД
        telegramUserService.registerOrUpdateUser(userId, username, firstName, lastName);

        UUID sessionId = gameService.createSession(userId, chatId, gameMode).getId();
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

    /**
     * Получает статистику пользователя
     * GET /api/game/user-stats?userId=xxx
     *
     * @param userId ID пользователя Telegram
     * @return DTO с полной статистикой пользователя
     */
    @GetMapping("/user-stats")
    public ResponseEntity<UserStatsDTO> getUserStats(@RequestParam Long userId) {
        log.info("Getting stats for user: {}", userId);
        TelegramUser user = telegramUserService.getUserStats(userId);
        return ResponseEntity.ok(new UserStatsDTO(user));
    }

    /**
     * Обновляет статистику пользователя после игры
     * POST /api/game/update-stats
     *
     * @param userId         ID пользователя Telegram
     * @param totalScore     Общее количество очков
     * @param correctAnswers Количество правильных ответов
     * @param wrongAnswers   Количество неправильных ответов
     * @param currentStreak  Текущая серия правильных ответов
     * @param bestStreak     Рекордная серия правильных ответов
     * @return Обновленный DTO со статистикой пользователя
     */
    @PostMapping("/update-stats")
    public ResponseEntity<UserStatsDTO> updateStats(
            @RequestParam Long userId,
            @RequestParam int totalScore,
            @RequestParam int correctAnswers,
            @RequestParam int wrongAnswers,
            @RequestParam int currentStreak,
            @RequestParam int bestStreak) {

        log.info("Updating stats for user: {} (score: {})", userId, totalScore);

        telegramUserService.updateStats(userId, totalScore, correctAnswers,
                wrongAnswers, currentStreak, bestStreak);
        TelegramUser user = telegramUserService.getUserStats(userId);

        return ResponseEntity.ok(new UserStatsDTO(user));
    }

    /**
     * Обновляет режим игры для существующей сессии
     * PATCH /api/game/session/{sessionId}/mode?gameMode=xxx
     *
     * @param sessionId ID игровой сессии
     * @param gameMode  Новый режим игры: "name" или "department"
     * @return пустой ответ с кодом 200 при успешном обновлении
     */
    @PatchMapping("/session/{sessionId}/mode")
    public ResponseEntity<?> updateGameMode(
            @PathVariable UUID sessionId,
            @RequestParam String gameMode) {
        log.info("Updating game mode for session: {} to {}", sessionId, gameMode);
        gameService.updateGameMode(sessionId, gameMode);
        return ResponseEntity.ok().build();
    }
}