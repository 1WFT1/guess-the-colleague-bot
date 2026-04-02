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

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Employee Controller", description = "API для управления сотрудниками")
public class EmployeeController {

    private final EmployeeService employeeService;

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

    @Operation(summary = "Получить всех активных сотрудников")
    @GetMapping
    public ResponseEntity<List<Employee>> getAllActive() {
        return ResponseEntity.ok(employeeService.getAllActiveEmployees());
    }

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
}