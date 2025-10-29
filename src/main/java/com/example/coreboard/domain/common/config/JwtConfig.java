package com.example.coreboard.domain.common.config;

import com.example.coreboard.domain.common.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {
    @Value("${jwt.secret.key}")
    private String jwtSecretKey;

    @PostConstruct
    public void init() {
        JwtUtil.init(jwtSecretKey);
    }
}

