package com.wft.guesscolleague.repository;

import com.wft.guesscolleague.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с сотрудниками (таблица employees)
 * Предоставляет методы для доступа к данным сотрудников
 */
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    /**
     * Находит всех активных сотрудников
     * @return список сотрудников с isActive = true
     */
    List<Employee> findByIsActiveTrue();

    /**
     * Подсчитывает количество активных сотрудников
     * @return количество сотрудников с isActive = true
     */
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.isActive = true")
    long countByIsActiveTrue();

    /**
     * Мягкое удаление сотрудника (установка isActive = false)
     * @param id ID сотрудника
     */
    @Modifying  // Указывает, что запрос изменяет данные
    @Transactional  // Операция выполняется в транзакции
    @Query("UPDATE Employee e SET e.isActive = false WHERE e.id = :id")
    void updateActiveStatus(@Param("id") UUID id);
}