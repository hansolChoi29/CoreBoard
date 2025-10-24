package com.example.coreboard.domain.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

// 테스트 안에서만 임시로 환경설정
// @SpringBootTest(properties = { "키=값" }) , yml 임시 주입
@SpringBootTest(properties = {
        "aes.secret.key=1234567890ABCDEF" // 16바이트 키 (AES-128용)
})
@ContextConfiguration(classes = EmailPhoneNumberEncode.class) // 이 빈만 등록
@ExtendWith(SpringExtension.class)
class EmailPhoneNumberEncodeTest {
    // 모키토 시반이 아니라서 순수 단위테스트로 가야함
    // 레포 안 씀, DB 안 씀, 빈 안 쓰기 때문 (협력자가 없다..)

    @Autowired
    private EmailPhoneNumberEncode encoder;

    @Test
    @DisplayName("암호화를_복호화하여_원본_텍스트_반환")
    void encryptThenDecrpty() {
        // encrypt() -> decrypt() 원본이 복원이 되는가?

        //given
        String original = "abc@naver.com";

        // when
        String encrypted = encoder.encrypt(original); // encrypt() 로 암호화
        String devrypted = encoder.decrypt(encrypted); // decrypt() 로 복호화

        // then
        assertNotNull(encrypted); // 암호화 결과가 null 안됨
        assertFalse(encrypted.isBlank()); // 공백 문자열 암됨
        assertNotEquals(original, encrypted); // 암호문이 원문과 달라야 정상
        assertEquals(original, devrypted); // 복호화 결과가 원본과 같아야 정상
    }

    @Test
    @DisplayName("동일한_입력_두_번_결과_다른_암호문")
    void encryptSameInputTwice() {
        // 같은 값을 두 번 암호화해도 결과가 달라야 정상
        String plain = "0101341234"; // 평문
        String encrypt1 = encoder.encrypt(plain);
        String encrypt2 = encoder.encrypt(plain);

        assertNotEquals(encrypt1, encrypt2); // 결과가 서로 달라야 함
    }
}