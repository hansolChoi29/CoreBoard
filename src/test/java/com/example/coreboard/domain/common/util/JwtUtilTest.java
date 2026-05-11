package com.example.coreboard.domain.common.util;

import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    private static final String TEST_SECRET = "jwtjwtjwtVeryVeryVeryVeryVeryVeryLongTooLongLongLongLongLongjwtVeryLong";

    private static SecretKey localTestKey;

    @BeforeAll
    static void setUpJwt() {
        JwtUtil.init(TEST_SECRET);
        localTestKey = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("토큰에_role_claim이_없으면_인증예외_발생")
    void getRoleWithoutRoleClaim() {
        String tokenWithoutRole = Jwts.builder()
                .setSubject("tester")
                .claim("userId", 10L)
                .claim("type", "access")
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 30))
                .signWith(localTestKey, SignatureAlgorithm.HS256)
                .compact();

        AuthErrorException exception = assertThrows(
                AuthErrorException.class,
                () -> JwtUtil.getRole(tokenWithoutRole)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }
}