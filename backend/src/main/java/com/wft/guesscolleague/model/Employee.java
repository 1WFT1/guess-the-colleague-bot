package com.wft.guesscolleague.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Модель сотрудника компании
 * Хранит информацию о сотрудниках для игры "Угадай коллегу"
 */
@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    /**
     * Уникальный идентификатор сотрудника
     * Автоматически генерируется как UUID
     */
    @Id  // Указывает, что это первичный ключ
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Полное имя сотрудника (Фамилия Имя)
     * Обязательное поле, не может быть null
     */
    @Column(nullable = false)
    private String fullName;

    /**
     * Отдел, в котором работает сотрудник
     * Например: Разработка, Маркетинг, HR, Аналитика
     */
    private String department;

    /**
     * URL ссылка на фото сотрудника
     * Может хранить путь к локальному файлу или внешнюю ссылку
     */
    @Column(name = "photo_url")
    private String photoUrl;

    /**
     * Статус активности сотрудника
     * true - сотрудник участвует в игре
     * false - сотрудник скрыт (не появляется в вопросах)
     */
    @Column(name = "is_active")
    private boolean isActive = true;

    /**
     * Дата и время создания записи
     * Автоматически заполняется при создании, нельзя изменить
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления записи
     * Автоматически обновляется при каждом изменении
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}