package com.example.coreboard.domain.config;

import org.springframework.beans.factory.annotation.Value;

import java.security.Key;

public class JwtUtil {
// 유틸은 정적(어노테이션 쓰면 안됨)이어야 하며 토큰생성 전용이어야 한다.

    @Value("${jwt.secret.key}")
    private String secretKey;

    private Key key;


    //토큰 생성/검증 메서드 필요


}
