package com.example.coreboard.config;

import com.example.coreboard.domain.common.config.EmailPhoneNumberEncode;
import org.junit.jupiter.api.Test;

public class EmailPhoneNumberEncodeTest {

    EmailPhoneNumberEncode encoded = new EmailPhoneNumberEncode();

    @Test
    void testEmailEncryptDecrypt() {
        // 이메일 암호화
        String email = "abc@naver.com";
        String encodeEmail = encoded.encrypt(email);
        System.out.println("이메일 암호화!!!!: " + encodeEmail);

        // 이메일 복호화
        String decodedEmail = encoded.decrypt(encodeEmail);
        System.out.println("이메일 복호화!!!!!: " + decodedEmail);

        // 전화번호 암호화
        String poneNum = "01012341234";
        String encodePhoneNum = encoded.encrypt(poneNum);
        System.out.println("전화번호 암호화!!!!: " + encodePhoneNum);
        // 전화번호 복호화
        String decodedPhoneNum = encoded.decrypt(encodePhoneNum);
        System.out.println("전화번호 복호화!!!!: " + decodedPhoneNum);
    }
}
