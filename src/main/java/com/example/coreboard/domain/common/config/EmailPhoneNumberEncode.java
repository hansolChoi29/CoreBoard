package com.example.coreboard.domain.common.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class EmailPhoneNumberEncode {
    // 비밀번호는 단방향이라 같이 못 함
    // 이메일 및 전화번호는 양방향 AES, RSA(암호화->복호화 가능)

    @Value("${aes.secret.key}")
    private String secretKey; //16글자

    @Value("${aes.algorithm}")
    private String algorithm;

    // SecretKeySpec : 키 만들기 - 문자열 키를 AES용 키 객체로 변환
    // Cipher.getInstance() : 암호 도구 생성 - AES 방식의 암호기 생성
    // cipher.init() : 모드 설정 - 암호화(ENCRYPT_MODE), 복호화(DECRYPT_MODE)
    // doFinal() : 실제 처리 - 평문을 암호문으로/ 암호문을 평문으로
    // Base64 : 문자열로 변환 - byte 데이터를 DB에 저장 가능한 문자열로 변환

    // 암호화
    public String encrypt(String plainText) { // plainText : 사용자가 입력한 평문(01012341234, abc@qwe.com)
        try {
            // 문자열 형태의 시크릿키를 암호화 키 객체 SecretKeySpec로 바꿈
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), algorithm);
            // Cipher 객체 생성(AES 알고리즘으로 암호화)
            Cipher cipher = Cipher.getInstance(algorithm);
            // 암호화 초기화 (암호화 = ENCRYPT_MODE)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            // 실제 암호화 수행 (encrypted는 사람이 읽을 수 없는 이진 데이터)
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            // Base64 인코딩 : 암호화된 byte[]를 Base64로 문자열로 변환(DB 저장용)
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("암호화 실패!: " + e.getMessage());
        }
    }

    // 복호화
    public String decrypt(String encryptedText) { // encryptedText : 암호화 되어 저장된 문자열(DB에서 꺼내옴)
        try {
            // 암호화 할 때 사용한 키로 다시 SecretKeySpec 생성
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), algorithm);
            // Cipher 객체를 복호화 모드로
            Cipher cipher = Cipher.getInstance(algorithm);
            // 복호화 모드로 초기화 (DECRYPT_MODE = 복호화)
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            // Base64로 인코딩된 문자열을 원래 byte[]로 복원
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            // byte[] 데이터를 복호화 (암호문을 평문으로)
            byte[] decrypted = cipher.doFinal(decoded);
            // 복호화된 byte[]를 문자열로 변환하여 반환
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("복호화 실패!: " + e.getMessage());
        }
    }
}
