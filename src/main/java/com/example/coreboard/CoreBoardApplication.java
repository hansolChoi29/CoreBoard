package com.example.coreboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class CoreBoardApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreBoardApplication.class, args);
    }
}
