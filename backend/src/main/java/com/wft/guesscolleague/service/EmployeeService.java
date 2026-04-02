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

/**
 * Сервис для работы с сотрудниками
 * Предоставляет методы для получения, сохранения и удаления сотрудников
 */
@Service
@RequiredArgsConstructor  // Lombok: создает конструктор для final полей
@Slf4j  // Lombok: добавляет логгер (log.info, log.error)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    /**
     * Получает всех активных сотрудников
     * Результат кэшируется на 5 минут (см. CacheConfig)
     * @return список активных сотрудников
     */
    @Cacheable(value = "employees", key = "'active'")
    public List<Employee> getAllActiveEmployees() {
        log.info("Loading ALL employees from database (temporary for admin)");
        // Временно возвращаем всех, чтобы админ видел всех
        return employeeRepository.findAll();
    }

    /**
     * Получает случайного активного сотрудника
     * @return случайный сотрудник
     * @throws IllegalStateException если нет активных сотрудников
     */
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

    /**
     * Получает N случайных активных сотрудников, исключая указанного
     * Используется для генерации дистракторов (неправильных вариантов ответов)
     *
     * @param excludeId ID сотрудника, которого нужно исключить (правильный ответ)
     * @param count количество нужных сотрудников
     * @return список случайных сотрудников
     */
    public List<Employee> getRandomActiveEmployees(UUID excludeId, int count) {
        List<Employee> activeEmployees = getAllActiveEmployees();
        log.info("Getting {} random employees excluding ID: {}", count, excludeId);

        // Исключаем правильный ответ
        List<Employee> candidates = activeEmployees.stream()
                .filter(e -> !e.getId().equals(excludeId))
                .collect(Collectors.toList());

        log.info("Candidates count: {}", candidates.size());

        // Если недостаточно сотрудников, возвращаем сколько есть
        if (candidates.size() < count) {
            log.warn("Not enough active employees: {} available, need {}", candidates.size(), count);
            return candidates;
        }

        // Перемешиваем и берем первых count
        Collections.shuffle(candidates);
        List<Employee> selected = candidates.subList(0, count);
        log.info("Selected {} employees", selected.size());
        return selected;
    }

    /**
     * Получает сотрудника по ID
     */
    public Employee getEmployeeById(UUID id) {
        return employeeRepository.findById(id).orElse(null);
    }

    /**
     * Сохраняет нового сотрудника (или обновляет существующего)
     * При изменении очищает кэш сотрудников
     * @param employee сотрудник для сохранения
     * @return сохраненный сотрудник
     */
    @Transactional
    @CacheEvict(value = "employees", allEntries = true)  // Очищает весь кэш employees
    public Employee saveEmployee(Employee employee) {
        log.info("Saving employee: {}", employee.getFullName());
        return employeeRepository.save(employee);
    }

    /**
     * Мягкое удаление сотрудника (установка isActive = false)
     * При изменении очищает кэш сотрудников
     * @param id ID сотрудника
     */
    @Transactional
    @CacheEvict(value = "employees", allEntries = true)
    public void deleteEmployee(UUID id) {
        log.info("Soft deleting employee: {}", id);
        employeeRepository.updateActiveStatus(id);
    }

    /**
     * Получает количество активных сотрудников
     * @return количество активных сотрудников
     */
    public long countActiveEmployees() {
        long count = employeeRepository.countByIsActiveTrue();
        log.info("Active employees count: {}", count);
        return count;
    }
}