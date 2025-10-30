package com.example.coreboard.domain.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(properties = {
        "aes.secret.key=short" // 암호화 실패 메시지 보기위해 분리하여 test
})
@ContextConfiguration(classes = EmailPhoneNumberManager.class)
@ExtendWith(SpringExtension.class)
public class EmailPhoneNubmerEncryptFailTest {

    @Autowired
    private EmailPhoneNumberManager encoder;

    @Test
    @DisplayName("암호화_실패_예외")
    void encryptException() {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                // Expected java.lang.RuntimeException to be thrown, but nothing was thrown.
                // 실행했는데 에외 안나고 정상 - encrypt 조건 없어서 그럼
                () -> encoder.encrypt("01012341234") //암호화 시도
        );
        assertTrue(exception.getMessage().contains("암호화 실패!"));
    }
}
