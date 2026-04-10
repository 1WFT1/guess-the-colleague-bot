package com.wft.guesscolleague.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wft.guesscolleague.config.TelegramConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Сервис для взаимодействия с Telegram API
 * Предоставляет методы для получения информации о пользователях из Telegram
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramApiService {

    private final TelegramConfig telegramConfig;  // Конфигурация Telegram бота
    private final RestTemplate restTemplate;      // HTTP клиент для запросов
    private final ObjectMapper objectMapper;      // Для парсинга JSON ответов

    /**
     * Получение информации о пользователе из Telegram API
     * Использует метод getChat для получения данных о чате/пользователе
     *
     * @param userId ID пользователя в Telegram
     * @return объект TelegramUserInfo с данными пользователя (имя, фамилия, username)
     */
    public TelegramUserInfo getUserInfo(Long userId) {
        try {
            // Формируем URL для запроса к Telegram Bot API
            String url = String.format("https://api.telegram.org/bot%s/getChat?chat_id=%d",
                    telegramConfig.getBotToken(), userId);

            log.debug("Requesting user info from Telegram API for userId: {}", userId);

            // Выполняем GET запрос
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode json = objectMapper.readTree(response.getBody());

            // Проверяем успешность ответа
            if (json.has("ok") && json.get("ok").asBoolean()) {
                JsonNode result = json.get("result");

                TelegramUserInfo userInfo = TelegramUserInfo.builder()
                        .userId(userId)
                        .username(result.has("username") ? result.get("username").asText() : null)
                        .firstName(result.has("first_name") ? result.get("first_name").asText() : null)
                        .lastName(result.has("last_name") ? result.get("last_name").asText() : null)
                        .build();

                log.debug("Successfully retrieved user info: {}", userInfo);
                return userInfo;
            } else {
                log.warn("Telegram API returned error for userId {}: {}", userId, json);
            }
        } catch (Exception e) {
            log.error("Failed to get user info from Telegram for userId {}: {}", userId, e.getMessage());
        }

        // Возвращаем объект только с userId, если не удалось получить данные
        return TelegramUserInfo.builder()
                .userId(userId)
                .build();
    }

    /**
     * DTO для передачи информации о пользователе Telegram
     * Используется для внутреннего обмена данными
     */
    @lombok.Builder
    @lombok.Data
    public static class TelegramUserInfo {
        private Long userId;      // ID пользователя в Telegram
        private String username;  // Username пользователя (без @)
        private String firstName; // Имя пользователя
        private String lastName;  // Фамилия пользователя
    }
}