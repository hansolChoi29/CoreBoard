package com.example.coreboard.domain.common.config;

import com.example.coreboard.domain.common.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;

public class JwtConfig {
    @Value("${jwt.secret.key}") // 문자열 : 비밀 값 원재료
    private String jwt;

    @PostConstruct // 의존성 주입이 이루어진 후 초기화를 수행하는 메서드, 생성자 보다 늦게 호출됨
    public void init() {
        JwtUtil.init(jwt);
    }

}
// 1) "문자열" 선언 - 초기화 시 사용 (SecretKey 객체를 만들기 위한 원재료)
// 초기화 하는 이유 : 우리가 가진 건 단지 문자열 뿐, JWT 라이브러리가 이해하고 쓸 수 없다.
// static 블록에서 한 번만 실행되고 문자열을 SecretKey 객체로 변환한다.
// 그 결과 Key 객체가 JWT 발급/검증에 바로 사용 가능해진다.

// 2) 시크릿키 "객체" 생성 - JWT 발급/ 검증 시 사용

// 3) JWT 토큰 생성 및 검증 <= JwtUtil 안에서 액세스/리프레시 토큰 발급, 검증