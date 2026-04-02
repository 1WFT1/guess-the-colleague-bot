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
@Entity  // Указывает, что этот класс является сущностью JPA (будет сохранен в БД)
@Table(name = "employees")  // Имя таблицы в базе данных
@Data  // Lombok: генерирует геттеры, сеттеры, toString, equals, hashCode
@NoArgsConstructor  // Lombok: генерирует конструктор без параметров
@AllArgsConstructor  // Lombok: генерирует конструктор со всеми параметрами
public class Employee {

    /**
     * Уникальный идентификатор сотрудника
     * Автоматически генерируется как UUID
     */
    @Id  // Указывает, что это первичный ключ
    @GeneratedValue(strategy = GenerationType.UUID)  // Автоматическая генерация UUID
    private UUID id;

    /**
     * Полное имя сотрудника (Фамилия Имя)
     * Обязательное поле, не может быть null
     */
    @Column(nullable = false)  // Поле не может быть null в БД
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
    @Column(name = "photo_url")  // Имя колонки в БД - "photo_url"
    private String photoUrl;

    /**
     * Статус активности сотрудника
     * true - сотрудник участвует в игре
     * false - сотрудник скрыт (не появляется в вопросах)
     */
    @Column(name = "is_active")  // Имя колонки в БД - "is_active"
    private boolean isActive = true;  // По умолчанию активен

    /**
     * Дата и время создания записи
     * Автоматически заполняется при создании, нельзя изменить
     */
    @CreationTimestamp  // Hibernate автоматически заполняет при создании
    @Column(name = "created_at", updatable = false)  // Нельзя обновлять после создания
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления записи
     * Автоматически обновляется при каждом изменении
     */
    @UpdateTimestamp  // Hibernate автоматически обновляет при изменении
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Версия для оптимистичной блокировки
     * Используется для предотвращения одновременного обновления одной записи
     * Автоматически увеличивается при каждом обновлении
     */
    //@Version  // JPA версионирование для оптимистичной блокировки
    //private Long version;
}