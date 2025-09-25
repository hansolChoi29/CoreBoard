package com.example.coreboard.domain.common.config;


import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
// 유틸은 정적(어노테이션 쓰면 안됨)이어야 하며 토큰생성 전용이어야 한다.

    private static final long ACCESS_TOKEN = 1000L * 60 * 30;
    private static final long REFRESH_TOKEN = 1000L * 60 * 60 * 24 * 7;
    private static SecretKey secretKey;
    //토큰 생성/검증 메서드 필요
    // 프로바이더는 jwt를 생성하고 파싱하는 클래스.
    // 그래서 프로바이더를 통해 어세스/리프레시 토큰을 생성하고
    // 생성된 토큰을 파싱해서 클래임으로 반환하는 메서드여야 함

    // 초기화
    public static void init(String secret) {
        secretKey = key.hmacShaKeyFor(secret.getBytes());
    }

    // AccessToken
    public static String createAccessToken(Long userId, String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("typ", "access")
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // RefreshToken
    public static String createRefreshToken(Long userId, String username) {
        return Jwts.builder()
                .setSubject("refresh")
                .claim("userId", userId)
                .claim("typ", "refresh")
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 유효성 검사
    public static boolean validationToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigninKey(secretKey)
                    .build()
                    .parserClaimJws(token);
        } catch (Exception e) {
            System.out.println("토큰 만료!" + e.getMessage());
        } catch (Exception e) {
            System.out.println("토큰 검증 실패!" + e.getMessage());
        }
        return false;
    }

    public static String getUsername(String token){
        return getClaim(token);
    }

    public static Claims getClaim()

}
