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
        "aes.secret.key=short"
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
                () -> encoder.encrypt("01012341234")
        );
        assertTrue(exception.getMessage().contains("암호화 실패!"));
    }
}
