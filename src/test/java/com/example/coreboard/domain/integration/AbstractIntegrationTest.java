package com.example.coreboard.domain.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;


public abstract class AbstractIntegrationTest {
    protected static final MySQLContainer<?> mysql;

    static {
        mysql = new MySQLContainer<>("mysql:8.0-debian")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);
        mysql.start();
    }

    @DynamicPropertySource
    static void overrideDataSourceProps(
            DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }
}
