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
import com.wft.guesscolleague.repository.GameSessionRepository;
import com.wft.guesscolleague.repository.QuestionAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final EmployeeService employeeService;
    private final GameSessionRepository sessionRepository;
    private final QuestionAttemptRepository attemptRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public GameSession createSession(Long userId, Long chatId) {
        log.info("Creating session for user: {}", userId);

        // Проверяем, есть ли активная сессия
        Optional<GameSession> existingSession = sessionRepository.findByUserIdAndIsActiveTrue(userId);
        if (existingSession.isPresent()) {
            GameSession session = existingSession.get();
            session.setLastActivity(LocalDateTime.now());
            log.info("Using existing session: {}", session.getId());
            return sessionRepository.save(session);
        }

        GameSession session = new GameSession();
        session.setUserId(userId);
        session.setChatId(chatId);
        session.setLastActivity(LocalDateTime.now());
        GameSession saved = sessionRepository.save(session);
        log.info("Created new session: {}", saved.getId());
        return saved;
    }

    @Transactional
    public QuestionDTO generateNextQuestion(UUID sessionId) {
        log.info("Generating next question for session: {}", sessionId);

        GameSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    log.error("Session not found: {}", sessionId);
                    return new IllegalArgumentException("Session not found");
                });

        if (!session.isActive()) {
            log.error("Session is not active: {}", sessionId);
            throw new IllegalStateException("Session is not active");
        }

        // Проверяем, что есть достаточно сотрудников
        long activeEmployeesCount = employeeService.countActiveEmployees();
        log.info("Active employees count: {}", activeEmployeesCount);

        if (activeEmployeesCount < 4) {
            String error = String.format("Not enough active employees. Need at least 4, but found: %d", activeEmployeesCount);
            log.error(error);
            throw new IllegalStateException(error);
        }

        try {
            // Выбираем случайного сотрудника
            Employee correctEmployee = employeeService.getRandomActiveEmployee();
            log.info("Correct employee: {} (ID: {})", correctEmployee.getFullName(), correctEmployee.getId());

            // Получаем дистракторов
            List<Employee> distractors = employeeService.getRandomActiveEmployees(
                    correctEmployee.getId(), 3);
            log.info("Found {} distractors", distractors.size());

            // Создаем варианты ответов
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

            // Перемешиваем
            Collections.shuffle(options);
            log.info("Shuffled options count: {}", options.size());

            // Сохраняем вопрос
            QuestionAttempt attempt = new QuestionAttempt();
            attempt.setSession(session);
            attempt.setEmployee(correctEmployee);

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

            // Формируем DTO для фронтенда
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

    @Transactional
    public AnswerResponse processAnswer(AnswerRequest request) {
        log.info("Processing answer for question: {}", request.getQuestionId());

        QuestionAttempt attempt = attemptRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("Question attempt not found"));

        // Проверяем, что на вопрос еще не отвечали
        if (attempt.getIsCorrect() != null) {
            throw new IllegalStateException("Question already answered");
        }

        // Получаем варианты ответов
        List<Map<String, Object>> options;
        try {
            options = objectMapper.readValue(attempt.getOptions(), new TypeReference<List<Map<String, Object>>>() {});
            log.info("Parsed {} options", options.size());
        } catch (JsonProcessingException e) {
            log.error("Failed to parse options", e);
            throw new RuntimeException("Failed to parse options", e);
        }

        // Проверяем индекс ответа
        if (request.getSelectedOptionIndex() < 0 || request.getSelectedOptionIndex() >= options.size()) {
            throw new IllegalArgumentException("Invalid option index");
        }

        // Проверяем правильность ответа
        UUID correctEmployeeId = attempt.getEmployee().getId();
        String selectedEmployeeIdStr = (String) options.get(request.getSelectedOptionIndex()).get("employeeId");
        UUID selectedEmployeeId = UUID.fromString(selectedEmployeeIdStr);
        boolean isCorrect = correctEmployeeId.equals(selectedEmployeeId);

        log.info("Answer check - Correct: {}, Selected: {}", correctEmployeeId, selectedEmployeeId);

        // Рассчитываем изменение баллов
        int pointsDelta = isCorrect ? 5 : -6;

        // Обновляем сессию
        GameSession session = attempt.getSession();
        session.addScore(pointsDelta);

        if (isCorrect) {
            session.incrementCorrect();
        } else {
            session.incrementWrong();
        }

        session.setLastActivity(LocalDateTime.now());
        sessionRepository.save(session);

        // Обновляем попытку
        attempt.setSelectedOption(request.getSelectedOptionIndex());
        attempt.setIsCorrect(isCorrect);
        attempt.setPointsDelta(pointsDelta);
        attemptRepository.save(attempt);

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