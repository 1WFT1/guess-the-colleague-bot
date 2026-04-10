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
import com.wft.guesscolleague.service.TelegramUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
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
    private final TelegramUserService telegramUserService;

    /**
     * Создает новую игровую сессию
     */
    @Transactional
    public GameSession createSession(Long userId, Long chatId, String gameMode) {
        log.info("Creating session for user: {} with gameMode: {}", userId, gameMode);

        TelegramUser user = telegramUserService.getUserStats(userId);
        telegramUserService.incrementGamesPlayed(userId);

        // Закрываем старые сессии
        Optional<GameSession> oldSessionOpt = sessionRepository.findByUserIdAndIsActiveTrue(userId);
        if (oldSessionOpt.isPresent()) {
            GameSession old = oldSessionOpt.get();
            old.setActive(false);
            sessionRepository.save(old);
        }

        GameSession session = new GameSession();
        session.setUserId(userId);
        session.setChatId(chatId);
        session.setLastActivity(LocalDateTime.now());
        session.setTotalScore(user.getTotalScore());
        session.setCorrectAnswers(user.getCorrectAnswers());
        session.setWrongAnswers(user.getWrongAnswers());
        session.setCurrentStreak(user.getCurrentStreak());
        session.setBestStreak(user.getBestStreak());
        session.setActive(true);
        session.setGameMode(gameMode);  // <-- КЛЮЧЕВОЕ: сохраняем режим

        GameSession saved = sessionRepository.save(session);
        log.info("Created session: {} with gameMode: {}", saved.getId(), gameMode);
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
    public QuestionDTO generateNextQuestion(UUID sessionId) {
        GameSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        String gameMode = session.getGameMode();
        log.info("Generating question for session: {} with gameMode: {}", sessionId, gameMode);

        if ("department".equals(gameMode)) {
            log.info("Generating DEPARTMENT question");
            return generateDepartmentQuestion(session);
        } else {
            log.info("Generating NAME question");
            return generateNameQuestion(session);
        }
    }

    private QuestionDTO generateNameQuestion(GameSession session) {
        Employee correctEmployee = employeeService.getRandomActiveEmployee();
        List<Employee> distractors = employeeService.getRandomActiveEmployees(correctEmployee.getId(), 3);

        List<Map<String, Object>> options = new ArrayList<>();

        Map<String, Object> correctOption = new HashMap<>();
        correctOption.put("employeeId", correctEmployee.getId().toString());
        correctOption.put("fullName", correctEmployee.getFullName());
        options.add(correctOption);

        for (Employee d : distractors) {
            Map<String, Object> option = new HashMap<>();
            option.put("employeeId", d.getId().toString());
            option.put("fullName", d.getFullName());
            options.add(option);
        }

        Collections.shuffle(options);

        QuestionAttempt attempt = new QuestionAttempt();
        attempt.setSession(session);
        attempt.setEmployee(correctEmployee);
        attempt.setQuestionType("name");  // Важно: устанавливаем тип

        try {
            String optionsJson = objectMapper.writeValueAsString(options);
            attempt.setOptions(optionsJson);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize options", e);
            throw new RuntimeException("Failed to serialize options", e);
        }

        attemptRepository.save(attempt);

        List<String> optionNames = options.stream()
                .map(opt -> (String) opt.get("fullName"))
                .collect(Collectors.toList());

        return new QuestionDTO(attempt.getId(), correctEmployee.getPhotoUrl(), optionNames);
    }

    private QuestionDTO generateDepartmentQuestion(GameSession session) {
        Employee correctEmployee = employeeService.getRandomActiveEmployee();
        String correctDepartment = correctEmployee.getDepartment();

        // Получаем все уникальные отделы
        List<String> allDepartments = employeeService.getAllActiveEmployees()
                .stream()
                .map(Employee::getDepartment)
                .filter(dept -> dept != null && !dept.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        List<String> options = new ArrayList<>();
        options.add(correctDepartment);

        List<String> otherDepartments = allDepartments.stream()
                .filter(d -> !d.equals(correctDepartment))
                .collect(Collectors.toList());

        Collections.shuffle(otherDepartments);
        for (int i = 0; i < Math.min(3, otherDepartments.size()); i++) {
            options.add(otherDepartments.get(i));
        }

        while (options.size() < 4) {
            options.add("Другой отдел");
        }

        Collections.shuffle(options);

        QuestionAttempt attempt = new QuestionAttempt();
        attempt.setSession(session);
        attempt.setEmployee(correctEmployee);
        attempt.setQuestionType("department");

        try {
            String optionsJson = objectMapper.writeValueAsString(options);
            attempt.setOptions(optionsJson);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize options", e);
            throw new RuntimeException("Failed to serialize options", e);
        }

        attemptRepository.save(attempt);

        return new QuestionDTO(attempt.getId(), correctEmployee.getPhotoUrl(), options);
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

        QuestionAttempt attempt = attemptRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("Question attempt not found"));

        if (attempt.getIsCorrect() != null) {
            throw new IllegalStateException("Question already answered");
        }

        String questionType = attempt.getQuestionType();
        if (questionType == null) {
            questionType = "name";
        }

        log.info("Question type: {}", questionType);

        boolean isCorrect;
        String correctAnswerText;

        if ("department".equals(questionType)) {
            // ========== ЛОГИКА ДЛЯ ВОПРОСОВ ОБ ОТДЕЛАХ ==========
            List<String> options;
            try {
                options = objectMapper.readValue(attempt.getOptions(), new TypeReference<List<String>>() {});
                log.info("Parsed {} department options: {}", options.size(), options);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse department options", e);
                throw new RuntimeException("Failed to parse options", e);
            }

            if (request.getSelectedOptionIndex() < 0 || request.getSelectedOptionIndex() >= options.size()) {
                throw new IllegalArgumentException("Invalid option index");
            }

            String selectedDepartment = options.get(request.getSelectedOptionIndex());
            String correctDepartment = attempt.getEmployee().getDepartment();

            isCorrect = correctDepartment.equals(selectedDepartment);
            correctAnswerText = correctDepartment;

            log.info("Department answer - Correct: {}, Selected: {}", correctDepartment, selectedDepartment);

        } else {
            // ========== ЛОГИКА ДЛЯ ВОПРОСОВ ОБ ИМЕНАХ ==========
            List<Map<String, Object>> options;
            try {
                options = objectMapper.readValue(attempt.getOptions(), new TypeReference<List<Map<String, Object>>>() {});
                log.info("Parsed {} name options", options.size());
            } catch (JsonProcessingException e) {
                log.error("Failed to parse name options", e);
                throw new RuntimeException("Failed to parse options", e);
            }

            if (request.getSelectedOptionIndex() < 0 || request.getSelectedOptionIndex() >= options.size()) {
                throw new IllegalArgumentException("Invalid option index");
            }

            UUID correctEmployeeId = attempt.getEmployee().getId();
            String selectedEmployeeIdStr = (String) options.get(request.getSelectedOptionIndex()).get("employeeId");
            UUID selectedEmployeeId = UUID.fromString(selectedEmployeeIdStr);

            isCorrect = correctEmployeeId.equals(selectedEmployeeId);
            correctAnswerText = attempt.getEmployee().getFullName();

            log.info("Name answer - Correct: {}, Selected: {}", correctEmployeeId, selectedEmployeeId);
        }

        // ========== ОБЩАЯ ЛОГИКА НАЧИСЛЕНИЯ ОЧКОВ ==========
        GameSession session = attempt.getSession();
        int currentScore = session.getTotalScore();
        int pointsDelta;
        String message;

        if (isCorrect) {
            pointsDelta = 5;
            int newScore = currentScore + pointsDelta;
            session.setTotalScore(newScore);
            session.incrementCorrect();

            int newStreak = session.getCurrentStreak() + 1;
            session.setCurrentStreak(newStreak);
            if (newStreak > session.getBestStreak()) {
                session.setBestStreak(newStreak);
            }

            message = "Верно! +5 баллов";
            log.info("Correct answer! +5 points. New score: {}", session.getTotalScore());
        } else {
            if (currentScore >= 6) {
                pointsDelta = -6;
                int newScore = currentScore + pointsDelta;
                session.setTotalScore(newScore);
                message = "Ошибка! -6 баллов. Правильно: " + correctAnswerText;
                log.info("Wrong answer! -6 points. New score: {}", session.getTotalScore());
            } else if (currentScore > 0 && currentScore < 6) {
                pointsDelta = -currentScore;
                session.setTotalScore(0);
                message = "Ошибка! -" + currentScore + " баллов. Правильно: " + correctAnswerText;
                log.info("Wrong answer! -{} points (had only {}). New score: 0", currentScore, currentScore);
            } else {
                pointsDelta = 0;
                session.setTotalScore(0);
                message = "Ошибка! Правильно: " + correctAnswerText + " (у вас 0 очков, штраф не применен)";
                log.info("Wrong answer! No points to deduct. Score remains 0");
            }
            session.setCurrentStreak(0);
            session.incrementWrong();
        }

        session.setLastActivity(LocalDateTime.now());
        sessionRepository.save(session);

        telegramUserService.updateScore(session.getUserId(), session.getTotalScore());

        attempt.setSelectedOption(request.getSelectedOptionIndex());
        attempt.setIsCorrect(isCorrect);
        attempt.setPointsDelta(pointsDelta);
        attemptRepository.save(attempt);

        log.info("Answer processed. Correct: {}, PointsDelta: {}, New score: {}",
                isCorrect, pointsDelta, session.getTotalScore());

        return new AnswerResponse(
                isCorrect,
                pointsDelta,
                session.getTotalScore(),
                correctAnswerText,
                message
        );
    }

    // Очистка старых неактивных сессий (старше 7 дней)
    @Scheduled(cron = "0 0 3 * * ?") // Каждый день в 3 часа ночи
    @Transactional
    public void cleanupOldSessions() {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        int deleted = sessionRepository.deleteOldInactiveSessions(weekAgo);
        log.info("Deleted {} old inactive sessions", deleted);
    }

    // Очистка старых вопросов (старше 30 дней)
    @Scheduled(cron = "0 0 4 * * ?")
    @Transactional
    public void cleanupOldQuestions() {
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
        int deleted = attemptRepository.deleteOldAttempts(monthAgo);
        log.info("Deleted {} old question attempts", deleted);
    }

    @Transactional
    public void updateGameMode(UUID sessionId, String gameMode) {
        GameSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        session.setGameMode(gameMode);
        sessionRepository.save(session);
        log.info("Updated session {} gameMode to {}", sessionId, gameMode);
    }

}