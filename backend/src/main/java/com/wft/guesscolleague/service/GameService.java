package com.wft.guesscolleague.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wft.guesscolleague.dto.AnswerRequest;
import com.wft.guesscolleague.dto.AnswerResponse;
import com.wft.guesscolleague.dto.QuestionDTO;
import com.wft.guesscolleague.model.Employee;
import com.wft.guesscolleague.model.GameSession;
import com.wft.guesscolleague.model.QuestionAttempt;
import com.wft.guesscolleague.model.TelegramUser;
import com.wft.guesscolleague.repository.GameSessionRepository;
import com.wft.guesscolleague.repository.QuestionAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Основной игровой сервис
 * Содержит всю бизнес-логику игры: создание сессий, генерацию вопросов, обработку ответов
 */
@Service
@RequiredArgsConstructor  // Lombok: конструктор для final полей
@Slf4j  // Lombok: логгер
public class GameService {

    private final EmployeeService employeeService;
    private final GameSessionRepository sessionRepository;
    private final QuestionAttemptRepository attemptRepository;
    private final ObjectMapper objectMapper;
    private final TelegramUserService telegramUserService;  // Добавить

    /**
     * Создает новую игровую сессию
     */
    @Transactional
    public GameSession createSession(Long userId, Long chatId) {
        log.info("Creating session for user: {}", userId);

        // Регистрируем пользователя (без данных, они придут с фронтенда)
        telegramUserService.registerOrUpdateUser(userId);

        // Увеличиваем счетчик игр
        telegramUserService.incrementGamesPlayed(userId);

        // Закрываем старые сессии
        Optional<GameSession> oldSessionOpt = sessionRepository.findByUserIdAndIsActiveTrue(userId);
        if (oldSessionOpt.isPresent()) {
            GameSession old = oldSessionOpt.get();
            old.setActive(false);
            sessionRepository.save(old);
            log.info("Closed old session: {}", old.getId());
        }

        // Создаем новую сессию с нулевыми очками
        GameSession session = new GameSession();
        session.setUserId(userId);
        session.setChatId(chatId);
        session.setLastActivity(LocalDateTime.now());
        session.setTotalScore(0);
        session.setCorrectAnswers(0);
        session.setWrongAnswers(0);
        session.setActive(true);

        GameSession saved = sessionRepository.save(session);
        log.info("Created new session: {} with score 0", saved.getId());
        return saved;
    }

    /**
     * Генерирует следующий вопрос для игровой сессии
     * Алгоритм:
     * 1. Выбирает случайного сотрудника (правильный ответ)
     * 2. Выбирает 3 случайных других сотрудников (дистракторы)
     * 3. Перемешивает варианты ответов
     * 4. Сохраняет вопрос в БД
     * 5. Возвращает DTO для фронтенда
     *
     * @param sessionId ID игровой сессии
     * @return DTO с вопросом (фото + варианты ответов)
     */
    @Transactional
    public QuestionDTO generateNextQuestion(UUID sessionId) {
        log.info("Generating next question for session: {}", sessionId);

        // Проверяем существование сессии
        GameSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    log.error("Session not found: {}", sessionId);
                    return new IllegalArgumentException("Session not found");
                });

        // Проверяем активность сессии
        if (!session.isActive()) {
            log.error("Session is not active: {}", sessionId);
            throw new IllegalStateException("Session is not active");
        }

        // Проверяем, что есть достаточно сотрудников (минимум 4 для 1 правильного + 3 дистрактора)
        long activeEmployeesCount = employeeService.countActiveEmployees();
        log.info("Active employees count: {}", activeEmployeesCount);

        if (activeEmployeesCount < 4) {
            String error = String.format("Not enough active employees. Need at least 4, but found: %d", activeEmployeesCount);
            log.error(error);
            throw new IllegalStateException(error);
        }

        try {
            // 1. Выбираем случайного сотрудника (правильный ответ)
            Employee correctEmployee = employeeService.getRandomActiveEmployee();
            log.info("Correct employee: {} (ID: {})", correctEmployee.getFullName(), correctEmployee.getId());

            // 2. Выбираем 3 случайных дистрактора (неправильные варианты)
            List<Employee> distractors = employeeService.getRandomActiveEmployees(
                    correctEmployee.getId(), 3);
            log.info("Found {} distractors", distractors.size());

            // 3. Создаем список вариантов ответов
            List<Map<String, Object>> options = new ArrayList<>();

            // Добавляем правильный ответ
            Map<String, Object> correctOption = new HashMap<>();
            correctOption.put("employeeId", correctEmployee.getId().toString());
            correctOption.put("fullName", correctEmployee.getFullName());
            options.add(correctOption);

            // Добавляем дистракторов
            for (Employee d : distractors) {
                Map<String, Object> option = new HashMap<>();
                option.put("employeeId", d.getId().toString());
                option.put("fullName", d.getFullName());
                options.add(option);
            }

            // Перемешиваем варианты в случайном порядке
            Collections.shuffle(options);
            log.info("Shuffled options count: {}", options.size());

            // 4. Сохраняем вопрос в БД
            QuestionAttempt attempt = new QuestionAttempt();
            attempt.setSession(session);
            attempt.setEmployee(correctEmployee);

            // Сохраняем варианты как JSON (для последующей проверки)
            try {
                String optionsJson = objectMapper.writeValueAsString(options);
                attempt.setOptions(optionsJson);
                log.info("Saved options JSON: {}", optionsJson);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize options", e);
                throw new RuntimeException("Failed to serialize options", e);
            }

            attemptRepository.save(attempt);
            log.info("Saved question attempt: {}", attempt.getId());

            // 5. Формируем DTO для фронтенда
            List<String> optionNames = options.stream()
                    .map(opt -> (String) opt.get("fullName"))
                    .collect(Collectors.toList());

            QuestionDTO dto = new QuestionDTO(
                    attempt.getId(),
                    correctEmployee.getPhotoUrl(),
                    optionNames
            );

            log.info("Generated question DTO: {}", dto);
            return dto;

        } catch (Exception e) {
            log.error("Error generating question", e);
            throw new RuntimeException("Failed to generate question: " + e.getMessage(), e);
        }
    }

    /**
     * Обрабатывает ответ пользователя на вопрос
     * Алгоритм:
     * 1. Находит вопрос по ID
     * 2. Сравнивает выбранный вариант с правильным ответом
     * 3. Начисляет/списывает баллы (+5 за правильный, -6 за неправильный)
     * 4. Обновляет статистику сессии
     * 5. Сохраняет результат попытки
     *
     * @param request данные ответа (sessionId, questionId, selectedOptionIndex)
     * @return результат проверки ответа
     */
    @Transactional
    public AnswerResponse processAnswer(AnswerRequest request) {
        log.info("Processing answer for question: {}", request.getQuestionId());
        // Находим вопрос по ID
        QuestionAttempt attempt = attemptRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("Question attempt not found"));

        // Проверяем, что на вопрос еще не отвечали (защита от повторной отправки)
        if (attempt.getIsCorrect() != null) {
            throw new IllegalStateException("Question already answered");
        }

        // Восстанавливаем варианты ответов из JSON
        List<Map<String, Object>> options;
        try {
            options = objectMapper.readValue(attempt.getOptions(), new TypeReference<List<Map<String, Object>>>() {});
            log.info("Parsed {} options", options.size());
        } catch (JsonProcessingException e) {
            log.error("Failed to parse options", e);
            throw new RuntimeException("Failed to parse options", e);
        }

        // Проверяем корректность индекса ответа
        if (request.getSelectedOptionIndex() < 0 || request.getSelectedOptionIndex() >= options.size()) {
            throw new IllegalArgumentException("Invalid option index");
        }

        // Сравниваем выбранный вариант с правильным ответом
        UUID correctEmployeeId = attempt.getEmployee().getId();
        String selectedEmployeeIdStr = (String) options.get(request.getSelectedOptionIndex()).get("employeeId");
        UUID selectedEmployeeId = UUID.fromString(selectedEmployeeIdStr);
        boolean isCorrect = correctEmployeeId.equals(selectedEmployeeId);

        log.info("Answer check - Correct: {}, Selected: {}", correctEmployeeId, selectedEmployeeId);

        // Рассчитываем изменение баллов (+5 за правильный, -6 за неправильный)
        int pointsDelta = isCorrect ? 5 : -6;

        // Обновляем сессию
        GameSession session = attempt.getSession();
        session.addScore(pointsDelta);  // Добавляем/списываем баллы

        if (isCorrect) {
            session.incrementCorrect();  // Увеличиваем счетчик правильных ответов
        } else {
            session.incrementWrong();    // Увеличиваем счетчик неправильных ответов
        }

        session.setLastActivity(LocalDateTime.now());
        sessionRepository.save(session);

        // Сохраняем результат попытки
        attempt.setSelectedOption(request.getSelectedOptionIndex());
        attempt.setIsCorrect(isCorrect);
        attempt.setPointsDelta(pointsDelta);
        attemptRepository.save(attempt);

        // Формируем сообщение для пользователя
        String message = isCorrect ?
                "Верно! +5 баллов" :
                "Ошибка! -6 баллов. Правильно: " + attempt.getEmployee().getFullName();

        log.info("Answer processed. Correct: {}, Points: {}, New score: {}",
                isCorrect, pointsDelta, session.getTotalScore());

        return new AnswerResponse(
                isCorrect,
                pointsDelta,
                session.getTotalScore(),
                attempt.getEmployee().getFullName(),
                message
        );
    }
}