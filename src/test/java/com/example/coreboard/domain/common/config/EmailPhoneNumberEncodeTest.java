package com.example.coreboard.domain.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

// 테스트 안에서만 임시로 환경설정
// @SpringBootTest(properties = { "키=값" }) , yml 임시 주입
@SpringBootTest(properties = {
        "aes.secret.key=1234567890ABCDEF" // 16바이트 키 (AES-128용)
})
class EmailPhoneNumberEncodeTest {
    // 모키토 시반이 아니라서 순수 단위테스트로 가야함
    // 레포 안 씀, DB 안 씀, 빈 안 쓰기 때문 (협력자가 없다..)
    @Autowired
    private EmailPhoneNumberEncode encoder;

    @Test
    void encrypt() {
        //given
        // 리플릭션 : 클래스 안의 private 필드나 메서드에도 강제로 접근해서 읽거나 수정할 수 있음

        // private 필드 시크릿키에 테스트키 주입 16바이트 AES
        // when
        // then
    }

    @Test
    void decrypt() {
    }
}