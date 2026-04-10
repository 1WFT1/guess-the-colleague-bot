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

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Employee Controller", description = "API для управления сотрудниками")
public class EmployeeController {

    private final EmployeeService employeeService;

    // Для админ-панели - ВСЕ сотрудники
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        log.info("Getting ALL employees for admin panel");
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    // Для игры - ТОЛЬКО активные сотрудники (другой путь!)
    @GetMapping("/active")
    public ResponseEntity<List<Employee>> getActiveEmployees() {
        log.info("Getting ACTIVE employees for game");
        return ResponseEntity.ok(employeeService.getAllActiveEmployees());
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getCount() {
        long count = employeeService.countActiveEmployees();
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        response.put("message", "Found " + count + " active employees");
        response.put("status", "OK");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable UUID id) {
        Employee employee = employeeService.getEmployeeById(id);
        if (employee == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(employee);
    }

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

            employee.setDepartment(body.containsKey("department") && body.get("department") != null ?
                    body.get("department").toString() : "");
            employee.setPhotoUrl(body.containsKey("photoUrl") && body.get("photoUrl") != null ?
                    body.get("photoUrl").toString() : "");

            // Обработка active
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok().build();
    }

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

    @PostMapping("/upload-csv")
    public ResponseEntity<?> uploadCSV(@RequestParam("file") MultipartFile file) {
        log.info("Received CSV upload request");

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Файл пуст");
        }

        try {
            List<Employee> employees = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                boolean isFirstLine = true;

                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    String[] columns = line.split(",");
                    if (columns.length < 3) continue;

                    Employee employee = new Employee();
                    employee.setFullName(columns[0].trim());
                    employee.setDepartment(columns[1].trim());
                    employee.setPhotoUrl(columns[2].trim());
                    employee.setActive(true);

                    employees.add(employee);
                }
            }

            if (employees.isEmpty()) {
                return ResponseEntity.badRequest().body("Нет данных для импорта");
            }

            List<Employee> saved = employeeService.saveAll(employees);
            log.info("Uploaded {} employees via CSV", saved.size());
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            log.error("Failed to upload CSV", e);
            return ResponseEntity.badRequest().body("Ошибка загрузки CSV: " + e.getMessage());
        }
    }
}