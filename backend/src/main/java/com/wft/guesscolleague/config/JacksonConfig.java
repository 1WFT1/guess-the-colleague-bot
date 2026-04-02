package com.wft.guesscolleague.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Конфигурация Jackson для сериализации/десериализации JSON
 * Настраивает ObjectMapper для работы с датами и временем
 */
@Configuration
public class JacksonConfig {

    /**
     * Создает основной ObjectMapper для всего приложения
     *
     * @return настроенный ObjectMapper
     *
     * Особенности:
     * - JavaTimeModule: поддержка Java 8 дат и времени (LocalDateTime, LocalDate)
     * - WRITE_DATES_AS_TIMESTAMPS: отключено → даты пишутся как ISO строки (например, "2026-04-02T10:30:00")
     */
    @Bean
    @Primary  // Этот бин будет использоваться по умолчанию
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Регистрируем модуль для работы с Java 8 временем (LocalDateTime, LocalDate)
        mapper.registerModule(new JavaTimeModule());
        // Отключаем запись дат как таймстемпов (миллисекунды) → используем ISO формат
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}