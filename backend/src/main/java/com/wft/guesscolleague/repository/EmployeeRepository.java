package com.wft.guesscolleague.repository;

import com.wft.guesscolleague.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    List<Employee> findByIsActiveTrue();

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.isActive = true")
    long countByIsActiveTrue();

    @Modifying
    @Transactional
    @Query("UPDATE Employee e SET e.isActive = false WHERE e.id = :id")
    void updateActiveStatus(@Param("id") UUID id);
}