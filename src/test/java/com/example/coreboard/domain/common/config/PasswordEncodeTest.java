package com.example.coreboard.domain.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = PasswordEncodeTest.class) // 이 빈만 등록
@ExtendWith(SpringExtension.class)
class PasswordEncodeTest {

    // 1) 같은 입력 -> 매번 다른 해시여야 함
    // 2) 암호화 후 비교 시 true / 틀리면 false

    @Test
    @DisplayName("같은_입력값_다른_해시")
    void encrypt() {
        PasswordEncode encoder = new PasswordEncode();
        String hash1 = encoder.encrypt("1234zcv");
        String hash2 = encoder.encrypt("qwer098");

        assertNotEquals(hash1, hash2);
    }

    @Test
    @DisplayName("encrypt()된_비밀번호와_원문_일치")
    void matches() {
        PasswordEncode encoder = new PasswordEncode();
        String password = "qwer1234";
        String encoded = encoder.encrypt(password);

        assertTrue(encoder.matches(password, encoded)); // 맞으면 true
        assertFalse(encoder.matches("wrong", encoded)); // 틀리면 false
    }
}