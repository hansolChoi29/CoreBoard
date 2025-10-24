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
@ContextConfiguration(classes = JwtConfig.class) // 이 빈만 등록
@ExtendWith(SpringExtension.class)
class JwtConfigTest {
    // 서버가 켜질 때 스프링이 자동으로 JWT 비밀키를 준비시키는지 테스트

    @Test
    @DisplayName("자동으로_JWT_준비")
    void init() {
        String token = JwtUtil.createAccessToken(1L,"tester");

        assertNotNull(token); // 토큰이 널이면 안됨
        assertFalse(token.isBlank()); // 빈 문자열도 안됨
        // 여기까지 통과했다는 건 secretKey가 제대로 세팅되었다는 의미
    }
}