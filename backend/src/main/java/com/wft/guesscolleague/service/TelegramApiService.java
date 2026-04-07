package com.wft.guesscolleague.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wft.guesscolleague.config.TelegramConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramApiService {

    private final TelegramConfig telegramConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Получение информации о пользователе из Telegram
     */
    public TelegramUserInfo getUserInfo(Long userId) {
        try {
            String url = String.format("https://api.telegram.org/bot%s/getChat?chat_id=%d",
                    telegramConfig.getBotToken(), userId);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode json = objectMapper.readTree(response.getBody());

            if (json.has("ok") && json.get("ok").asBoolean()) {
                JsonNode result = json.get("result");
                return TelegramUserInfo.builder()
                        .userId(userId)
                        .username(result.has("username") ? result.get("username").asText() : null)
                        .firstName(result.has("first_name") ? result.get("first_name").asText() : null)
                        .lastName(result.has("last_name") ? result.get("last_name").asText() : null)
                        .build();
            }
        } catch (Exception e) {
            log.error("Failed to get user info from Telegram: {}", e.getMessage());
        }
        return TelegramUserInfo.builder()
                .userId(userId)
                .build();
    }

    @lombok.Builder
    @lombok.Data
    public static class TelegramUserInfo {
        private Long userId;
        private String username;
        private String firstName;
        private String lastName;
    }
}