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
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final EmployeeService employeeService;           // Сервис для работы с сотрудниками
    private final GameSessionRepository sessionRepository;   // Репозиторий игровых сессий
    private final QuestionAttemptRepository attemptRepository; // Репозиторий попыток ответов
    private final ObjectMapper objectMapper;                 // Для JSON сериализации/десериализации
    private final TelegramUserService telegramUserService;   // Сервис для работы с пользователями

    /**
     * Создает новую игровую сессию для пользователя
     *
     * @param userId   ID пользователя в Telegram
     * @param chatId   ID чата (опционально)
     * @param gameMode Режим игры: "name" - угадать сотрудника, "department" - угадать отдел
     * @return созданная игровая сессия
     */
    @Transactional
    public GameSession createSession(Long userId, Long chatId, String gameMode) {
        log.info("Creating session for user: {} with gameMode: {}", userId, gameMode);

        // Получаем текущую статистику пользователя
        TelegramUser user = telegramUserService.getUserStats(userId);
        // Увеличиваем счетчик сыгранных игр
        telegramUserService.incrementGamesPlayed(userId);

        // Закрываем предыдущие активные сессии пользователя
        Optional<GameSession> oldSessionOpt = sessionRepository.findByUserIdAndIsActiveTrue(userId);
        if (oldSessionOpt.isPresent()) {
            GameSession old = oldSessionOpt.get();
            old.setActive(false);
            sessionRepository.save(old);
        }

        // Создаем новую сессию с текущей статистикой пользователя
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
        session.setGameMode(gameMode);  // Сохраняем выбранный режим игры

        GameSession saved = sessionRepository.save(session);
        log.info("Created session: {} with gameMode: {}", saved.getId(), gameMode);
        return saved;
    }

    /**
     * Генерирует следующий вопрос для игровой сессии
     * Тип вопроса (имя сотрудника или отдел) определяется из режима сессии
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

    /**
     * Генерирует вопрос на угадывание имени сотрудника
     * Выбирает случайного сотрудника и 3 случайных дистракторов (неправильных варианта)
     *
     * @param session текущая игровая сессия
     * @return DTO с вопросом для фронтенда
     */
    private QuestionDTO generateNameQuestion(GameSession session) {
        // Выбираем правильного сотрудника
        Employee correctEmployee = employeeService.getRandomActiveEmployee();
        // Выбираем 3 случайных дистрактора
        List<Employee> distractors = employeeService.getRandomActiveEmployees(correctEmployee.getId(), 3);

        // Формируем варианты ответов с employeeId для проверки
        List<Map<String, Object>> options = new ArrayList<>();

        // Правильный ответ
        Map<String, Object> correctOption = new HashMap<>();
        correctOption.put("employeeId", correctEmployee.getId().toString());
        correctOption.put("fullName", correctEmployee.getFullName());
        options.add(correctOption);

        // Дистракторы
        for (Employee d : distractors) {
            Map<String, Object> option = new HashMap<>();
            option.put("employeeId", d.getId().toString());
            option.put("fullName", d.getFullName());
            options.add(option);
        }

        // Перемешиваем варианты
        Collections.shuffle(options);

        // Сохраняем вопрос в БД
        QuestionAttempt attempt = new QuestionAttempt();
        attempt.setSession(session);
        attempt.setEmployee(correctEmployee);
        attempt.setQuestionType("name");  // Устанавливаем тип вопроса

        try {
            String optionsJson = objectMapper.writeValueAsString(options);
            attempt.setOptions(optionsJson);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize options", e);
            throw new RuntimeException("Failed to serialize options", e);
        }

        attemptRepository.save(attempt);

        // Извлекаем только имена для отображения на фронтенде
        List<String> optionNames = options.stream()
                .map(opt -> (String) opt.get("fullName"))
                .collect(Collectors.toList());

        return new QuestionDTO(attempt.getId(), correctEmployee.getPhotoUrl(), optionNames);
    }

    /**
     * Генерирует вопрос на угадывание отдела сотрудника
     * Правильный ответ - отдел сотрудника, варианты - все существующие отделы
     *
     * @param session текущая игровая сессия
     * @return DTO с вопросом для фронтенда
     */
    private QuestionDTO generateDepartmentQuestion(GameSession session) {
        // Выбираем случайного сотрудника
        Employee correctEmployee = employeeService.getRandomActiveEmployee();
        String correctDepartment = correctEmployee.getDepartment();

        // Получаем все уникальные отделы из активных сотрудников
        List<String> allDepartments = employeeService.getAllActiveEmployees()
                .stream()
                .map(Employee::getDepartment)
                .filter(dept -> dept != null && !dept.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        // Формируем варианты ответов
        List<String> options = new ArrayList<>();
        options.add(correctDepartment);  // Правильный отдел

        // Добавляем 3 случайных других отдела
        List<String> otherDepartments = allDepartments.stream()
                .filter(d -> !d.equals(correctDepartment))
                .collect(Collectors.toList());

        Collections.shuffle(otherDepartments);
        for (int i = 0; i < Math.min(3, otherDepartments.size()); i++) {
            options.add(otherDepartments.get(i));
        }

        // Если не хватает отделов, добавляем заглушку
        while (options.size() < 4) {
            options.add("Другой отдел");
        }

        Collections.shuffle(options);

        // Сохраняем вопрос в БД
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
     *
     * Алгоритм:
     * 1. Находит вопрос по ID
     * 2. Определяет тип вопроса (имя/отдел)
     * 3. Сравнивает выбранный вариант с правильным ответом
     * 4. Начисляет/списывает баллы (+5 за правильный, -6 за неправильный, но не ниже 0)
     * 5. Обновляет статистику сессии (очки, серии, правильные/неправильные)
     * 6. Сохраняет результат попытки
     *
     * @param request данные ответа (sessionId, questionId, selectedOptionIndex)
     * @return результат проверки ответа
     */
    @Transactional
    public AnswerResponse processAnswer(AnswerRequest request) {
        log.info("Processing answer for question: {}", request.getQuestionId());

        // Находим вопрос в БД
        QuestionAttempt attempt = attemptRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("Question attempt not found"));

        // Защита от повторной отправки ответа
        if (attempt.getIsCorrect() != null) {
            throw new IllegalStateException("Question already answered");
        }

        // Определяем тип вопроса
        String questionType = attempt.getQuestionType();
        if (questionType == null) {
            questionType = "name";
        }
        log.info("Question type: {}", questionType);

        boolean isCorrect;
        String correctAnswerText;

        // ========== ОБРАБОТКА В ЗАВИСИМОСТИ ОТ ТИПА ВОПРОСА ==========
        if ("department".equals(questionType)) {
            // Логика для вопросов об отделах
            List<String> options;
            try {
                options = objectMapper.readValue(attempt.getOptions(), new TypeReference<List<String>>() {});
                log.info("Parsed {} department options: {}", options.size(), options);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse department options", e);
                throw new RuntimeException("Failed to parse options", e);
            }

            // Проверяем корректность индекса
            if (request.getSelectedOptionIndex() < 0 || request.getSelectedOptionIndex() >= options.size()) {
                throw new IllegalArgumentException("Invalid option index");
            }

            String selectedDepartment = options.get(request.getSelectedOptionIndex());
            String correctDepartment = attempt.getEmployee().getDepartment();

            isCorrect = correctDepartment.equals(selectedDepartment);
            correctAnswerText = correctDepartment;

            log.info("Department answer - Correct: {}, Selected: {}", correctDepartment, selectedDepartment);

        } else {
            // Логика для вопросов об именах
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

        // ========== ОБНОВЛЕНИЕ СТАТИСТИКИ СЕССИИ ==========
        GameSession session = attempt.getSession();
        int currentScore = session.getTotalScore();
        int pointsDelta;
        String message;

        if (isCorrect) {
            // Правильный ответ: +5 очков
            pointsDelta = 5;
            int newScore = currentScore + pointsDelta;
            session.setTotalScore(newScore);
            session.incrementCorrect();

            // Обновляем серию правильных ответов
            int newStreak = session.getCurrentStreak() + 1;
            session.setCurrentStreak(newStreak);
            if (newStreak > session.getBestStreak()) {
                session.setBestStreak(newStreak);
            }

            message = "Верно! +5 баллов";
            log.info("Correct answer! +5 points. New score: {}", session.getTotalScore());
        } else {
            // Неправильный ответ: списываем баллы с защитой от отрицательного счета
            if (currentScore >= 6) {
                pointsDelta = -6;
                int newScore = currentScore + pointsDelta;
                session.setTotalScore(newScore);
                message = "Ошибка! -6 баллов. Правильно: " + correctAnswerText;
                log.info("Wrong answer! -6 points. New score: {}", session.getTotalScore());
            } else if (currentScore > 0 && currentScore < 6) {
                // Если очков меньше 6, списываем только то, что есть
                pointsDelta = -currentScore;
                session.setTotalScore(0);
                message = "Ошибка! -" + currentScore + " баллов. Правильно: " + correctAnswerText;
                log.info("Wrong answer! -{} points (had only {}). New score: 0", currentScore, currentScore);
            } else {
                // Если очков 0, не списываем
                pointsDelta = 0;
                session.setTotalScore(0);
                message = "Ошибка! Правильно: " + correctAnswerText + " (у вас 0 очков, штраф не применен)";
                log.info("Wrong answer! No points to deduct. Score remains 0");
            }
            session.setCurrentStreak(0);  // Сбрасываем серию
            session.incrementWrong();
        }

        // Сохраняем обновления сессии
        session.setLastActivity(LocalDateTime.now());
        sessionRepository.save(session);

        // Обновляем общий счет пользователя
        telegramUserService.updateScore(session.getUserId(), session.getTotalScore());

        // Сохраняем результат попытки
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

    /**
     * Очистка старых неактивных сессий (старше 7 дней)
     * Запускается автоматически каждый день в 3 часа ночи
     * Удаляет сессии, которые были неактивны более 7 дней
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupOldSessions() {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        int deleted = sessionRepository.deleteOldInactiveSessions(weekAgo);
        log.info("Deleted {} old inactive sessions", deleted);
    }

    /**
     * Очистка старых вопросов (старше 30 дней)
     * Запускается автоматически каждый день в 4 часа ночи
     * Удаляет попытки ответов, созданные более 30 дней назад
     */
    @Scheduled(cron = "0 0 4 * * ?")
    @Transactional
    public void cleanupOldQuestions() {
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
        int deleted = attemptRepository.deleteOldAttempts(monthAgo);
        log.info("Deleted {} old question attempts", deleted);
    }

    /**
     * Обновляет режим игры в существующей сессии
     *
     * @param sessionId ID игровой сессии
     * @param gameMode  новый режим игры ("name" или "department")
     */
    @Transactional
    public void updateGameMode(UUID sessionId, String gameMode) {
        GameSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        session.setGameMode(gameMode);
        sessionRepository.save(session);
        log.info("Updated session {} gameMode to {}", sessionId, gameMode);
    }
}