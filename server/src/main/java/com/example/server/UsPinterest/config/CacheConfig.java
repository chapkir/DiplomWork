package com.example.server.UsPinterest.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.CacheControl;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@EnableTransactionManagement
public class CacheConfig {

    private static final String[] CACHE_NAMES = {
            "pins",
            "users",
            "boards",
            "search",
            "profiles",
            "comments",
            "notifications",
            "posts",
            "likes"
    };

    public static final CacheControl API_CACHE_CONTROL = CacheControl
            .maxAge(Duration.ofMinutes(5))
            .mustRevalidate()
            .cachePrivate();

    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(CACHE_NAMES);
        cacheManager.setCaffeine(caffeineCacheBuilder());
        cacheManager.setAllowNullValues(true);
        return cacheManager;
    }

    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .recordStats();
    }

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
        pinCacheManager.setAllowNullValues(true);
        return pinCacheManager;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }
}