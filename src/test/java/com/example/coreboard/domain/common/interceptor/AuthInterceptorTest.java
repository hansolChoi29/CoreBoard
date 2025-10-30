package com.example.coreboard.domain.common.interceptor;

import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class AuthInterceptorTest {
    private static final String TEST_SECRET = "jwtjwtjwtVeryVeryVeryVeryVeryVeryLongTooLongLongLongLongLongjwtVeryLong";
    private static SecretKey localTestKey;
    AuthInterceptor authInterceptor;
    MockHttpServletRequest mockHttpServletRequest;

    MockHttpServletResponse mockHttpServletResponse;

    HandlerMethod handlerMethod;

    @BeforeAll
    static void setUpJwt() {
        JwtUtil.init(TEST_SECRET);
        localTestKey = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        authInterceptor = new AuthInterceptor();
        mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletResponse = new MockHttpServletResponse();

        handlerMethod = new HandlerMethod(
                this,
                this.getClass().getDeclaredMethod("setUp")
        );
    }

    @Test
    @DisplayName("Authorization_헤더_없음_GET통과")
    void noHeader_getRequestIsPass() {
        mockHttpServletRequest.setMethod("GET");
        assertDoesNotThrow(
                () -> authInterceptor.preHandle(
                        mockHttpServletRequest,
                        mockHttpServletResponse,
                        handlerMethod
                )
        );
    }

    @Test
    @DisplayName("Authorization_헤더_없음_POST예외")
    void noHeader_postRequest_fail() {
        mockHttpServletRequest.setMethod("POST");
        assertThrows(
                AuthErrorException.class, () -> {
                    authInterceptor.preHandle(
                            mockHttpServletRequest,
                            mockHttpServletResponse,
                            handlerMethod
                    );

                }
        );
    }

    @Test
    @DisplayName("토큰_만료")
    void noHeader_tokenEnd() {
        String token = Jwts.builder()
                .setSubject("tester")
                .claim("userId", 10L)
                .claim("type", "refresh")
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(localTestKey, SignatureAlgorithm.HS256)
                .compact();
        mockHttpServletRequest.setMethod("POST");
        mockHttpServletRequest.addHeader("Authorization", "Bearer " + token);

        assertThrows(AuthErrorException.class, () -> {
            authInterceptor.preHandle(
                    mockHttpServletRequest,
                    mockHttpServletResponse,
                    handlerMethod
            );
        });
    }

    @Test
    @DisplayName("유효하지_않은_토큰_예외발생")
    void invalidToken_fali() {
        mockHttpServletRequest.setMethod("POST");
        mockHttpServletRequest.addHeader("Authorization", "Bearer " + "badToken");

        assertThrows(
                AuthErrorException.class, () -> {
                    authInterceptor.preHandle(
                            mockHttpServletRequest,
                            mockHttpServletResponse,
                            handlerMethod
                    );
                }
        );
    }

    @Test
    @DisplayName("유효한_토큰_username저장")
    void validToken_success() {

        String token = JwtUtil.createAccessToken(10L, "tester");
        mockHttpServletRequest.setMethod("POST");
        mockHttpServletRequest.addHeader("Authorization", "Bearer " + token);

        boolean result = authInterceptor.preHandle(
                mockHttpServletRequest,
                mockHttpServletResponse,
                handlerMethod);

        assertTrue(result);
        assertEquals("tester", mockHttpServletRequest.getAttribute("username"));
    }
}