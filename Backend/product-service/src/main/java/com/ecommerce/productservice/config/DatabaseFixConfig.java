package com.ecommerce.productservice.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseFixConfig {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void fixStatusColumn() {
        try {
            log.info("Attempting to alter 'status' column in 'products' table to VARCHAR(50)...");
            jdbcTemplate.execute("ALTER TABLE products MODIFY COLUMN status VARCHAR(50) NOT NULL;");
            log.info("Successfully altered 'status' column length.");
        } catch (Exception e) {
            log.warn("Could not alter status column. It might already be altered or table doesn't exist: {}", e.getMessage());
        }
    }
}
