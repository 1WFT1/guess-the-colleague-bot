package com.wft.guesscolleague.controller;

import com.wft.guesscolleague.model.Employee;
import com.wft.guesscolleague.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Контроллер для управления сотрудниками
 * Доступен по адресу: /api/employees
 * Используется в админ-панели для CRUD операций
 */
@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")  // Разрешает CORS запросы с фронтенда
@RequiredArgsConstructor
@Tag(name = "Employee Controller", description = "API для управления сотрудниками")
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * Получает количество активных сотрудников
     * GET /api/employees/count
     *
     * @return количество активных сотрудников
     */
    @Operation(summary = "Получить количество активных сотрудников")
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getCount() {
        long count = employeeService.countActiveEmployees();
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        response.put("message", "Found " + count + " active employees");
        response.put("status", "OK");
        return ResponseEntity.ok(response);
    }

    /**
     * Получает список всех активных сотрудников
     * GET /api/employees
     *
     * @return список всех активных сотрудников
     */
    @Operation(summary = "Получить всех активных сотрудников")
    @GetMapping
    public ResponseEntity<List<Employee>> getAllActive() {
        return ResponseEntity.ok(employeeService.getAllActiveEmployees());
    }

    /**
     * Получает сотрудника по ID
     * GET /api/employees/{id}
     *
     * @param id UUID сотрудника
     * @return данные сотрудника или 404 если не найден
     */
    @Operation(summary = "Получить сотрудника по ID")
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployee(
            @Parameter(description = "ID сотрудника", required = true)
            @PathVariable UUID id) {
        return employeeService.getAllActiveEmployees().stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Создает нового сотрудника
     * POST /api/employees
     *
     * @param employee данные нового сотрудника (fullName, department, photoUrl, isActive)
     * @return созданный сотрудник с присвоенным ID
     */
    @Operation(summary = "Создать нового сотрудника")
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        return ResponseEntity.ok(employeeService.saveEmployee(employee));
    }

    /**
     * Обновляет данные существующего сотрудника
     * PUT /api/employees/{id}
     *
     * @param id ID сотрудника для обновления
     * @param employee новые данные сотрудника
     * @return обновленный сотрудник
     */
    @Operation(summary = "Обновить сотрудника")
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(
            @Parameter(description = "ID сотрудника", required = true)
            @PathVariable UUID id,
            @RequestBody Employee employee) {
        employee.setId(id);  // Устанавливаем ID из пути
        return ResponseEntity.ok(employeeService.saveEmployee(employee));
    }

    /**
     * Удаляет сотрудника (мягкое удаление, установка isActive = false)
     * DELETE /api/employees/{id}
     *
     * @param id ID сотрудника для удаления
     * @return пустой ответ с кодом 200
     */
    @Operation(summary = "Удалить сотрудника")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(
            @Parameter(description = "ID сотрудника", required = true)
            @PathVariable UUID id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Переключает статус активности сотрудника
     * PATCH /api/employees/{id}/active
     *
     * @param id ID сотрудника
     * @param body тело запроса с полем isActive (true/false)
     * @return обновленный сотрудник
     */
    @Operation(summary = "Переключить статус активности сотрудника")
    @PatchMapping("/{id}/active")
    public ResponseEntity<Employee> toggleActive(
            @Parameter(description = "ID сотрудника", required = true)
            @PathVariable UUID id,
            @RequestBody Map<String, Boolean> body) {
        // Находим сотрудника
        Employee employee = employeeService.getAllActiveEmployees().stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElseThrow();
        // Устанавливаем новый статус активности
        employee.setActive(body.get("isActive"));
        // Сохраняем изменения
        return ResponseEntity.ok(employeeService.saveEmployee(employee));
    }
}