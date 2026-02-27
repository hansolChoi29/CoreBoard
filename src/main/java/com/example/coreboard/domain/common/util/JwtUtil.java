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
    private static final long ACCESS_TOKEN = 1000L * 60 * 30;
    private static final long REFRESH_TOKEN = 1000L * 60 * 60 * 24 * 7;
    private static SecretKey secretKey;

    public static void init(String secret) {
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public static String createAccessToken(Long userId, String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("type", "access")
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public static String createRefreshToken(Long userId, String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("type", "refresh")
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public static boolean validationToken(String accessToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(accessToken);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("토큰 만료!: " + e.getMessage());
        } catch (JwtException e) {
            System.out.println("토큰 검증 실패!: " + e.getMessage());
        }
        return false;
    }

    public static String getUsername(String accessToken) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(accessToken)
                .getBody()
                .getSubject();
    }

    public static Long getUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId", Long.class);
    }

    public static boolean validationRefreshToken(String token) {
        try {
            String type = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("type", String.class);
            return "refresh".equals(type);
        } catch (ExpiredJwtException e) {
            System.out.println("리프레시 토큰 만료!: " + e.getMessage());
        } catch (JwtException e) {
            System.out.println("리프레시 토큰 검증 실패!: " + e.getMessage());
        }
        return false;
    }
}
