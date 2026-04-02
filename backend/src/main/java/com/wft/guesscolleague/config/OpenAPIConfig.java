package com.wft.guesscolleague.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Конфигурация OpenAPI (Swagger) для документации API
 * После запуска документация доступна по адресу: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenAPIConfig {

    /**
     * Настройка OpenAPI документации
     *
     * @return конфигурация OpenAPI
     *
     * Содержит:
     * - Название и описание API
     * - Контактную информацию
     * - Лицензию
     * - URL сервера для тестирования
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Guess Colleague Game API")
                        .description("API для игры Угадай коллегу - Telegram Mini App")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Support")
                                .email("support@example.com")
                                .url("https://example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")
                ));
    }
}