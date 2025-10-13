package com.example.coreboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing // 자동으로 값을 등록하겠다.
@SpringBootApplication
public class CoreBoardApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreBoardApplication.class, args);
    }

}
