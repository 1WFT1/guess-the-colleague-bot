package com.wft.guesscolleague.controller;

import com.wft.guesscolleague.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/diagnostic")
@RequiredArgsConstructor
@Tag(name = "Diagnostic Controller", description = "Эндпоинты для диагностики")
public class DiagnosticController {

    private final EmployeeService employeeService;

    @Operation(summary = "Проверить статус приложения")
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("app", "Guess Colleague Game");
        status.put("version", "1.0.0");
        status.put("status", "RUNNING");

        try {
            long employeeCount = employeeService.countActiveEmployees();
            status.put("employeeCount", employeeCount);
            status.put("canStartGame", employeeCount >= 4);
            status.put("message", employeeCount >= 4 ?
                    "Ready to play" :
                    "Need at least 4 employees to start game");
        } catch (Exception e) {
            status.put("employeeCount", "ERROR");
            status.put("error", e.getMessage());
        }

        return ResponseEntity.ok(status);
    }

    @Operation(summary = "Получить список всех активных сотрудников с деталями")
    @GetMapping("/employees")
    public ResponseEntity<Map<String, Object>> getEmployeesDiagnostic() {
        Map<String, Object> response = new HashMap<>();
        try {
            var employees = employeeService.getAllActiveEmployees();
            response.put("count", employees.size());
            response.put("employees", employees);
            response.put("status", "OK");
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}