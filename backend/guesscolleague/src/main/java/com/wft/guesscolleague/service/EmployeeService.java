package com.wft.guesscolleague.service;

import com.wft.guesscolleague.model.Employee;
import com.wft.guesscolleague.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Cacheable(value = "employees", key = "'active'")
    public List<Employee> getAllActiveEmployees() {
        log.info("Loading active employees from database");
        List<Employee> employees = employeeRepository.findByIsActiveTrue();
        log.info("Found {} active employees", employees.size());
        return employees;
    }

    public Employee getRandomActiveEmployee() {
        List<Employee> activeEmployees = getAllActiveEmployees();
        if (activeEmployees.isEmpty()) {
            log.error("No active employees found!");
            throw new IllegalStateException("No active employees found");
        }

        int randomIndex = (int) (Math.random() * activeEmployees.size());
        Employee employee = activeEmployees.get(randomIndex);
        log.info("Selected random employee: {} (ID: {})", employee.getFullName(), employee.getId());
        return employee;
    }

    public List<Employee> getRandomActiveEmployees(UUID excludeId, int count) {
        List<Employee> activeEmployees = getAllActiveEmployees();
        log.info("Getting {} random employees excluding ID: {}", count, excludeId);

        List<Employee> candidates = activeEmployees.stream()
                .filter(e -> !e.getId().equals(excludeId))
                .collect(Collectors.toList());

        log.info("Candidates count: {}", candidates.size());

        if (candidates.size() < count) {
            log.warn("Not enough active employees: {} available, need {}", candidates.size(), count);
            // Если недостаточно, возвращаем сколько есть
            return candidates;
        }

        Collections.shuffle(candidates);
        List<Employee> selected = candidates.subList(0, count);
        log.info("Selected {} employees", selected.size());
        return selected;
    }

    @Transactional
    @CacheEvict(value = "employees", allEntries = true)
    public Employee saveEmployee(Employee employee) {
        log.info("Saving employee: {}", employee.getFullName());
        return employeeRepository.save(employee);
    }

    @Transactional
    @CacheEvict(value = "employees", allEntries = true)
    public void deleteEmployee(UUID id) {
        log.info("Soft deleting employee: {}", id);
        employeeRepository.updateActiveStatus(id);
    }

    public long countActiveEmployees() {
        long count = employeeRepository.countByIsActiveTrue();
        log.info("Active employees count: {}", count);
        return count;
    }
}