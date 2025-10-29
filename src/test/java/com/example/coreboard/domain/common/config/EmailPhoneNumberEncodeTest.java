package com.example.coreboard.domain.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "aes.secret.key=1234567890ABCDEF"
})
@ContextConfiguration(classes = EmailPhoneNumberEncode.class)
@ExtendWith(SpringExtension.class)
class EmailPhoneNumberEncodeTest {

    @Autowired
    private EmailPhoneNumberEncode encoder;

    @Test
    @DisplayName("암호화를_복호화하여_원본_텍스트_반환")
    void encryptThenDecrpty() {

        String original = "abc@naver.com";
        String encrypted = encoder.encrypt(original);
        String devrypted = encoder.decrypt(encrypted);

        assertNotNull(encrypted);
        assertFalse(encrypted.isBlank());
        assertNotEquals(original, encrypted);
        assertEquals(original, devrypted);
    }

    @Test
    @DisplayName("동일한_입력_두_번_결과_다른_암호문")
    void encryptSameInputTwice() {
        String plain = "0101341234";
        String encrypt1 = encoder.encrypt(plain);
        String encrypt2 = encoder.encrypt(plain);

        assertNotEquals(encrypt1, encrypt2);
    }

    @Test
    @DisplayName("복호화_불가한_문자열")
    void decryptException() {
        String invalidCipher = "이건진짜암호문아님";
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> encoder.decrypt(invalidCipher)
        );

        assertTrue(exception.getMessage().contains("복호화 실패"));
    }
}