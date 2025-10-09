package com.example.coreboard.domain.common.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class EmailPhoneNumberEncode {
    // [ IV(12바이트) ][ 암호문(나머지) ]

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12; // GCM 표준
    private static final int TAG_BIT_LENGTH = 128;

    @Value("${aes.secret.key}")
    private String secretKey; //16글자

    // SecretKeySpec : 키 만들기 - 문자열 키를 AES용 키 객체로 변환
    // Cipher.getInstance() : 암호 도구 생성 - AES 방식의 암호기 생성
    // cipher.init() : 모드 설정 - 암호화(ENCRYPT_MODE), 복호화(DECRYPT_MODE)
    // doFinal() : 실제 처리 - 평문을 암호문으로/ 암호문을 평문으로
    // Base64 : 문자열로 변환 - byte 데이터를 DB에 저장 가능한 문자열로 변환

    // 암호화
    public String encrypt(String plainText) { // plainText : 사용자가 입력한 평문(01012341234, abc@qwe.com)
        try {
            // iv는 암호문이 매번 달라지게 만드는 랜덤
            byte[] iv = new byte[IV_LENGTH]; // 빈 상자
            new SecureRandom().nextBytes(iv); // 랜덤 숫자 넣기

            Cipher cipher = Cipher.getInstance(TRANSFORMATION); //AES 알고리즘을 GCM모드로 패딩 없이 사용하겠다.

            // 비밀키 준비
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM); // 사람이 읽을 수 없는 문자열 키를 AES가 
            // 이해할 수 있는 비밀키 객체로 변환

            // 암호화 초기화 (암호화 = ENCRYPT_MODE)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(TAG_BIT_LENGTH, iv));

            // 실제 암호화 수행 (encrypted는 사람이 읽을 수 없는 이진 데이터)
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // IV+암호문 합치기 -> Base64 인코딩
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length); // 만들어 놓은 iv와 암호화를 넣기 위해 길이 설정해주고
            buffer.put(iv); // iv put
            buffer.put(encrypted); // 암호화 put

            // Base64 인코딩 : 암호화된 byte[]를 Base64로 문자열로 변환(DB 저장용)
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new RuntimeException("암호화 실패!: " + e.getMessage());
        }
    }

    // 복호화
    public String decrypt(String encryptedText) { // encryptedText : 암호화 되어 저장된 문자열(DB에서 꺼내옴)
        try {
            // 저장된 문자열을 바이트로 되돌리기
            byte[] allBytes = Base64.getDecoder().decode(encryptedText); // DB에는 암호화된 데이터가 그냥 byte[]로 저장될 수 없기 때문에 
            // 암호화 시 Base64 인코딩으로 문자열로 바꿔서 넣어놨음 그래서 다시 되돌리기
            ByteBuffer buffer = ByteBuffer.wrap(allBytes); // allByte는 iv+암호문이 한 덩어리로 들어있는데 앞에서부터 순서대로 조금씩 꺼내기위해 
            // ByteBuffer로 감쌈

            byte[] iv = new byte[IV_LENGTH]; // iv 담을 상자
            buffer.get(iv); // 암호화할 때 앞 부분에 붙여서 저장된 iv 꺼냄

            // 암호문 전체를 담을 크기만큼 빈 상자 만듦
            byte[] cipherBytes = new byte[buffer.remaining()]; // buffer.remaining() : 버퍼에 남아있는 바이트 수 알려줌
            buffer.get(cipherBytes); // cipherBytes에 담기

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            // 암호화 할 때 사용한 키로 다시 SecretKeySpec 생성
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);

            // 복호화 모드로 초기화 (DECRYPT_MODE = 복호화)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(TAG_BIT_LENGTH, iv));

            // Base64로 인코딩된 문자열을 원래 byte[]로 복원
            // byte[] 데이터를 복호화 (암호문을 평문으로)
            byte[] decrypted = cipher.doFinal(cipherBytes);

            // 복호화된 byte[]를 문자열로 변환하여 반환
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("복호화 실패!: " + e.getMessage());
        }
    }
}
