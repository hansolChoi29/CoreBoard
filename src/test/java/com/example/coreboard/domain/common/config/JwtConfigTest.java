package com.example.coreboard.domain.common.config;

import com.example.coreboard.domain.common.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "jwt.secret.key=this-is-a-very-very-long-test-secret-key-over-32byte"
})
@ContextConfiguration(classes = JwtConfig.class)
@ExtendWith(SpringExtension.class)
class JwtConfigTest {

    @Test
    @DisplayName("자동으로_JWT_준비")
    void init() {
        String token = JwtUtil.createAccessToken(1L, "tester");

        assertNotNull(token);
        assertFalse(token.isBlank());
    }
}