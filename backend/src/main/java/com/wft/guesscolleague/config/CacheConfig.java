package com.wft.guesscolleague.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Конфигурация кэширования для приложения
 * Использует Caffeine (высокопроизводительная библиотека кэширования)
 */
@Configuration
public class CacheConfig {

    /**
     * Настройка менеджера кэша
     *
     * @return CacheManager с настройками для кэша сотрудников
     *
     * Настройки:
     * - expireAfterWrite: данные устаревают через 5 минут после записи
     * - maximumSize: максимум 1000 записей в кэше
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("employees");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)  // Кэш живет 5 минут
                .maximumSize(1000));                    // Не более 1000 сотрудников
        return cacheManager;
    }
}