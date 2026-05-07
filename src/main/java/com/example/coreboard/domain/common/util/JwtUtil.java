package com.example.coreboard.domain.common.util;

import com.example.coreboard.domain.common.exception.auth.AuthErrorCode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.users.entity.UserRole;
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

    public static String createAccessToken(
            Long userId,
            String username,
            UserRole role
    ) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role.name())
                .claim("type", "access")
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public static String createRefreshToken(
            Long userId,
            String username,
            UserRole role
    ) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role.name())
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
        } catch (JwtException e) {
            return false;
        }
    }

    public static String getUsername(String accessToken) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(accessToken)
                .getBody()
                .getSubject();
    }

    public static UserRole getRole(String token) {
        String role = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
        if (role == null) {
            throw new AuthErrorException(AuthErrorCode.UNAUTHORIZED);
        }

        return UserRole.valueOf(role);
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
        } catch (JwtException e) {
            return false;
        }
    }
}
