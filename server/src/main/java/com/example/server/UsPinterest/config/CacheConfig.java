package com.example.server.UsPinterest.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Конфигурация кэширования с использованием Caffeine
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Определяет доступные кэши
     */
    private static final String[] CACHE_NAMES = {
            "pins",
            "users",
            "boards",
            "search",
            "profiles",
            "comments",
            "notifications"
    };

    /**
     * Создает и настраивает основной менеджер кэширования
     *
     * @return настроенный CacheManager
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(CACHE_NAMES);
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    /**
     * Конфигурирует Caffeine с настройками производительности
     *
     * @return настроенный билдер Caffeine
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .initialCapacity(100)     // Начальная емкость кэша
                .maximumSize(1000)        // Максимальное количество элементов
                .expireAfterWrite(10, TimeUnit.MINUTES) // Время жизни элемента после записи
                .expireAfterAccess(5, TimeUnit.MINUTES) // Время жизни элемента после последнего доступа
                .recordStats();           // Включение статистики кэша
    }

    /**
     * Создает отдельный кэш для пинов с более длительным сроком жизни
     *
     * @return настроенный CacheManager для пинов
     */
    @Bean(name = "extendedPinCacheManager")
    public CacheManager extendedPinCacheManager() {
        CaffeineCacheManager pinCacheManager = new CaffeineCacheManager("extended_pins");
        pinCacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .initialCapacity(200)
                        .maximumSize(2000)
                        .expireAfterWrite(30, TimeUnit.MINUTES)
                        .expireAfterAccess(15, TimeUnit.MINUTES)
                        .recordStats()
        );
        return pinCacheManager;
    }
}