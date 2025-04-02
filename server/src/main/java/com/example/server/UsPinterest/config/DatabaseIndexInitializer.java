package com.example.server.UsPinterest.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class DatabaseIndexInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseIndexInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initializeIndices() {
        logger.info("Начало инициализации индексов базы данных");
        try {
            Resource resource = new ClassPathResource("db/indices.sql");

            String sql;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                sql = reader.lines().collect(Collectors.joining("\n"));
            }

            String[] statements = sql.split(";");

            for (String statement : statements) {
                String trimmedStatement = statement.trim();
                if (!trimmedStatement.isEmpty()) {
                    try {
                        jdbcTemplate.execute(trimmedStatement);
                        logger.debug("Успешно выполнен SQL: {}", trimmedStatement);
                    } catch (Exception e) {
                        logger.error("Ошибка при выполнении SQL: {}", trimmedStatement, e);
                    }
                }
            }

            logger.info("Индексы базы данных успешно инициализированы");

            checkIndices();

        } catch (IOException e) {
            logger.error("Не удалось инициализировать индексы базы данных", e);
            throw new RuntimeException("Не удалось инициализировать индексы", e);
        }
    }

    private void checkIndices() {
        logger.info("Проверка созданных индексов:");

        String sql = "SELECT tablename, indexname FROM pg_indexes WHERE schemaname = 'public' ORDER BY tablename, indexname";

        jdbcTemplate.query(sql, (rs, rowNum) -> {
            String tableName = rs.getString("tablename");
            String indexName = rs.getString("indexname");
            logger.info("Таблица: {}, Индекс: {}", tableName, indexName);
            return null;
        });
    }
}