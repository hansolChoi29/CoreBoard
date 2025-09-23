package com.example.coreboard.domain.config;

import org.springframework.beans.factory.annotation.Value;

import java.security.Key;

public class JwtUtil {
// 유틸은 정적(어노테이션 쓰면 안됨)이어야 하며 토큰생성 전용이어야 한다.

    @Value("${jwt.secret.key}")
    private String secretKey;



    //토큰 생성/검증 메서드 필요

    // 프로바이더는 jwt를 생성하고 파싱하는 클래스.
    // 그래서 프로바이더를 통해 어세스/리프레시 토큰을 생성하고
    // 생성된 토큰을 파싱해서 클래임으로 반환하는 메서드여야 함


}
