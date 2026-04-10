package com.wft.guesscolleague.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class TelegramConfig {

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.bot.username:}")
    private String botUsername;

    @Value("${admin.user.ids:}")
    private String adminUserIds;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public List<Long> adminUserIds() {
        if (adminUserIds == null || adminUserIds.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(adminUserIds.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    public String getBotToken() {
        return botToken;
    }

    public String getBotUsername() {
        return botUsername;
    }
}