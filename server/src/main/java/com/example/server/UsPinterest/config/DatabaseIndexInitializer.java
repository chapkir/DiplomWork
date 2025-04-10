package com.example.server.UsPinterest.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DatabaseIndexInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseIndexInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    @Retryable(value = {SQLException.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void initializeIndices() {
        logger.info("Starting database index initialization");
        try {
            Resource resource = new ClassPathResource("db/indices.sql");

            String sql;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                sql = reader.lines().collect(Collectors.joining("\n"));
            }

            List<String> statements = Arrays.stream(sql.split(";"))
                    .map(String::trim)
                    .filter(stmt -> !stmt.isEmpty())
                    .collect(Collectors.toList());

            for (String statement : statements) {
                try {
                    jdbcTemplate.execute(statement);
                    logger.debug("Successfully executed SQL: {}", statement);
                } catch (Exception e) {
                    if (statement.contains("IF NOT EXISTS")) {
                        logger.warn("Non-critical error executing SQL (index may already exist): {}", e.getMessage());
                    } else {
                        logger.error("Error executing SQL: {}", statement, e);
                    }
                }
            }

            logger.info("Database indexes successfully initialized");

            if (logger.isDebugEnabled()) {
                checkIndices();
            }

        } catch (IOException e) {
            logger.error("Failed to initialize database indexes", e);
            throw new RuntimeException("Failed to initialize indexes", e);
        }
    }

    private void checkIndices() {
        logger.debug("Checking created indexes:");

        String sql = "SELECT tablename, indexname FROM pg_indexes WHERE schemaname = 'public' ORDER BY tablename, indexname";

        jdbcTemplate.query(sql, (rs, rowNum) -> {
            String tableName = rs.getString("tablename");
            String indexName = rs.getString("indexname");
            logger.debug("Table: {}, Index: {}", tableName, indexName);
            return null;
        });
    }
}