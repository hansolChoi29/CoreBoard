package com.example.coreboard.domain.common.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {
    // 유틸은 정적(어노테이션 쓰면 안됨)이어야 하며 토큰생성 전용이어야 한다.
    private static final long ACCESS_TOKEN = 1000L * 60 * 30;
    private static final long REFRESH_TOKEN = 1000L * 60 * 60 * 24 * 7;
    private static SecretKey secretKey; // JWT 라이브러리가 실제 서명/검증에 사용하는 도구
    // 문자열 없이 SecretKey 못 만들고
    // SecretKey 없이 JWT 발급/검증 못함


    public static void init(String secret) {
        // 문자열을 객체로 변환하기
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        // Keys : 문자열로 된 비밀키를 객체로 쉽게 만들어주는 유틸 클래스
        // hmacShaKeyFor : Key클래스 안의 정적 메서드(HMAC SHA 알고리즘)
        // secret.getBytes() : 문자열을 바이트 배열로 변환 (이유: HMAC SHA알고리즘은 바이트 배열 형태로 키를 필요로 함)
        // HMAC SHA알고리즘이란?
        // 핵심은 비밀 키로 토큰 변조를 막는다.
    }

    // 1) 어세스 토큰 발급 메서드
    public static String createAccessToken(Long userId, String username) {
        return Jwts.builder() // JWT 만들기 위한 빌더 객체 생성 (준비단계)
                .setSubject(username) // 토큰 내부 세팅 01
                .claim("userId", userId) // 토큰 내부 세팅 02
                .claim("type", "access") // 토큰 내부 세팅 03
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN))// 토큰 내부 세팅 04
                .signWith(secretKey, SignatureAlgorithm.HS256) // 핵심 코드: 알고리즘 사용해서 secretKeyr기반으로 payload를 암호화
                .compact(); // 핵심 코드: 빌더가 설정한 데이터 + 서명을 합쳐서 최종 JWT 문자열을 생성하겠다. (클라이언트에게 전달되는 토큰)
    }

    // 2) 리프레시 토큰 발급 메서드
    public static String createRefreshToken(Long userId, String username) {
        return Jwts.builder()
                .setSubject(username) // 토큰 내부 세팅 01
                .claim("userId", userId) // 토큰 내부 세팅 02
                .claim("type", "refresh") // 토큰 내부 세팅 03
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN))// 토큰 내부 세팅 04
                .signWith(secretKey, SignatureAlgorithm.HS256) // 핵심 코드: 알고리즘 사용해서 secretKeyr기반으로 payload를 암호화
                .compact(); // 핵심 코드: 빌더가 설정한 데이터 + 서명을 합쳐서 최종 JWT 문자열을 생성하겠다. (클라이언트에게 전달되는 토큰)
    }

    // 3) 토큰 검증 메서드 
    public static boolean validationToken(String accessToken) {
        try {
            Jwts.parserBuilder()// 토큰 검사 준비
                    .setSigningKey(secretKey)// 서버에서 만든 비밀키로 서명 확인
                    .build() // 검사기 만들기
                    .parseClaimsJws(accessToken); // 실제 토큰 검사 : 서명, 만료 체크
            return true; // 예외 없으면 유효한 토큰
        } catch (ExpiredJwtException e) {// ExpiredJwtException : jjwt에서 제공하는 예외, 유효시간 지났을 때 발생
            System.out.println("토큰 만료!: " + e.getMessage());
        } catch (JwtException e) { // JwtException : jjwt에서 제공하는 예외, 토큰이 올바르지 않거나 검증에 실패했을 때 발생
            System.out.println("토큰 검증 실패!: " + e.getMessage());
        }
        return false; // true면 통과, false면 실패
    }

    // username 추출
    public static String getUsername(String accessToken) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(accessToken)// parseClaimsJws : 토큰 해석하는 메서드
                .getBody() // Claim 객체 꺼내기
                .getSubject(); // username 추출
    }
}
