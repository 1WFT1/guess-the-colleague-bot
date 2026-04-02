package com.wft.guesscolleague.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для проверки работоспособности приложения
 * Используется для health checks (например, в Docker/Kubernetes)
 */
@RestController
public class HealthController {

    /**
     * Проверка здоровья приложения
     * GET /health
     *
     * @return статус UP если приложение работает
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Guess Colleague Bot is running!");
        return ResponseEntity.ok(response);
    }

    /**
     * Корневой эндпоинт
     * GET /
     *
     * @return базовая информация о сервисе
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, String>> home() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("service", "Guess Colleague Backend");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }
}