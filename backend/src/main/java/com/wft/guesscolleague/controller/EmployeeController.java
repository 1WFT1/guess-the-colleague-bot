package com.wft.guesscolleague.controller;

import com.wft.guesscolleague.model.Employee;
import com.wft.guesscolleague.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Контроллер для управления сотрудниками
 * Предоставляет REST API для CRUD операций с сотрудниками
 * Доступен по адресу: /api/employees
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Employee Controller", description = "API для управления сотрудниками")
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * Получить всех сотрудников (для админ-панели)
     * GET /api/employees
     *
     * @return список всех сотрудников (и активных, и неактивных)
     */
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        log.info("Getting ALL employees for admin panel");
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    /**
     * Получить только активных сотрудников (для игры)
     * GET /api/employees/active
     *
     * @return список только активных сотрудников
     */
    @GetMapping("/active")
    public ResponseEntity<List<Employee>> getActiveEmployees() {
        log.info("Getting ACTIVE employees for game");
        return ResponseEntity.ok(employeeService.getAllActiveEmployees());
    }

    /**
     * Получить количество активных сотрудников
     * GET /api/employees/count
     *
     * @return количество активных сотрудников
     */
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
     * Получить сотрудника по ID
     * GET /api/employees/{id}
     *
     * @param id UUID сотрудника
     * @return данные сотрудника или 404 если не найден
     */
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable UUID id) {
        Employee employee = employeeService.getEmployeeById(id);
        if (employee == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(employee);
    }

    /**
     * Создать нового сотрудника
     * POST /api/employees
     *
     * @param body данные сотрудника в формате JSON
     * @return созданный сотрудник с присвоенным ID
     */
    @PostMapping
    public ResponseEntity<?> createEmployee(@RequestBody Map<String, Object> body) {
        log.info("=== CREATE EMPLOYEE ===");
        log.info("Received body: {}", body);

        try {
            Employee employee = new Employee();

            // Проверяем обязательные поля
            if (!body.containsKey("fullName") || body.get("fullName") == null) {
                return ResponseEntity.badRequest().body("fullName is required");
            }
            employee.setFullName(body.get("fullName").toString());

            // Устанавливаем необязательные поля (если отсутствуют - пустая строка)
            employee.setDepartment(body.containsKey("department") && body.get("department") != null ?
                    body.get("department").toString() : "");
            employee.setPhotoUrl(body.containsKey("photoUrl") && body.get("photoUrl") != null ?
                    body.get("photoUrl").toString() : "");

            // Обработка статуса активности (по умолчанию true)
            if (body.containsKey("active") && body.get("active") != null) {
                Object activeValue = body.get("active");
                if (activeValue instanceof Boolean) {
                    employee.setActive((Boolean) activeValue);
                } else if (activeValue instanceof String) {
                    employee.setActive(Boolean.parseBoolean((String) activeValue));
                } else {
                    employee.setActive(true);
                }
            } else {
                employee.setActive(true);
            }

            Employee saved = employeeService.saveEmployee(employee);
            log.info("Created employee: {}", saved.getId());
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            log.error("Error creating employee", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Обновить данные сотрудника
     * PUT /api/employees/{id}
     *
     * @param id ID сотрудника для обновления
     * @param body новые данные сотрудника
     * @return обновленный сотрудник
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        log.info("=== UPDATE EMPLOYEE ===");
        log.info("Updating employee: {}", id);
        log.info("Received body: {}", body);

        try {
            Employee existingEmployee = employeeService.getEmployeeById(id);
            if (existingEmployee == null) {
                log.error("Employee not found: {}", id);
                return ResponseEntity.notFound().build();
            }

            // Обновляем поля с проверкой на null
            if (body.containsKey("fullName") && body.get("fullName") != null) {
                existingEmployee.setFullName(body.get("fullName").toString());
            }

            if (body.containsKey("department")) {
                existingEmployee.setDepartment(body.get("department") != null ? body.get("department").toString() : "");
            }

            if (body.containsKey("photoUrl")) {
                existingEmployee.setPhotoUrl(body.get("photoUrl") != null ? body.get("photoUrl").toString() : "");
            }

            if (body.containsKey("active")) {
                Object activeValue = body.get("active");
                if (activeValue != null) {
                    if (activeValue instanceof Boolean) {
                        existingEmployee.setActive((Boolean) activeValue);
                    } else if (activeValue instanceof String) {
                        existingEmployee.setActive(Boolean.parseBoolean((String) activeValue));
                    }
                } else {
                    existingEmployee.setActive(false);
                }
            }

            Employee saved = employeeService.saveEmployee(existingEmployee);
            log.info("Employee updated: {}", saved.getId());
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            log.error("Error updating employee", e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Удалить сотрудника (мягкое удаление, установка isActive = false)
     * DELETE /api/employees/{id}
     *
     * @param id ID сотрудника для удаления
     * @return пустой ответ с кодом 200
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Переключить статус активности сотрудника
     * PATCH /api/employees/{id}/active
     *
     * @param id ID сотрудника
     * @param body тело запроса с полем active (true/false)
     * @return обновленный сотрудник
     */
    @PatchMapping("/{id}/active")
    public ResponseEntity<?> toggleActive(@PathVariable UUID id, @RequestBody Map<String, Boolean> body) {
        log.info("Toggling active status for employee: {}", id);
        log.info("Request body: {}", body);

        try {
            Employee employee = employeeService.getEmployeeById(id);
            if (employee == null) {
                log.error("Employee not found: {}", id);
                return ResponseEntity.notFound().build();
            }

            // Поддержка обоих форматов: "active" и "isActive"
            Boolean isActive = body.get("isActive");
            if (isActive == null) {
                isActive = body.get("active");
            }

            if (isActive == null) {
                log.error("No active status provided");
                return ResponseEntity.badRequest().body("active status is required");
            }

            employee.setActive(isActive);
            Employee saved = employeeService.saveEmployee(employee);
            log.info("Employee {} active status changed to: {}", saved.getId(), saved.isActive());

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            log.error("Error toggling active status", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Загрузить сотрудников из CSV файла
     * POST /api/employees/upload-csv
     *
     * Ожидается файл с колонками: ФИО, Отдел, Фото URL
     * Первая строка считается заголовком и пропускается
     *
     * @param file CSV файл для загрузки
     * @return список сохраненных сотрудников
     */
    @PostMapping("/upload-csv")
    public ResponseEntity<?> uploadCSV(@RequestParam("file") MultipartFile file) {
        log.info("Received CSV upload request");

        // Проверяем, что файл не пустой
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Файл пуст");
        }

        try {
            List<Employee> employees = new ArrayList<>();

            // Читаем CSV файл построчно
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                boolean isFirstLine = true;

                while ((line = reader.readLine()) != null) {
                    // Пропускаем первую строку (заголовок)
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    // Разделяем строку на колонки
                    String[] columns = line.split(",");
                    if (columns.length < 3) continue; // Пропускаем некорректные строки

                    Employee employee = new Employee();
                    employee.setFullName(columns[0].trim());
                    employee.setDepartment(columns[1].trim());
                    employee.setPhotoUrl(columns[2].trim());
                    employee.setActive(true); // Новые сотрудники активны по умолчанию

                    employees.add(employee);
                }
            }

            // Проверяем, что есть данные для импорта
            if (employees.isEmpty()) {
                return ResponseEntity.badRequest().body("Нет данных для импорта");
            }

            // Сохраняем всех сотрудников
            List<Employee> saved = employeeService.saveAll(employees);
            log.info("Uploaded {} employees via CSV", saved.size());
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            log.error("Failed to upload CSV", e);
            return ResponseEntity.badRequest().body("Ошибка загрузки CSV: " + e.getMessage());
        }
    }
}