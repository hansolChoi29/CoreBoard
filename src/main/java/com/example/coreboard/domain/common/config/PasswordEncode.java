package com.example.coreboard.domain.common.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

@Component
public class PasswordEncode {
    private static final int ITERATIONS = 85319; // 반복 횟수
    private static final int KEY_LENGTH = 128;   // 키 길이, 비트 단위 (128bit)

    @Value("${algorithm}") // 환경변수 값 주입
    private String algorithm;

    // 소금 만들기
    public byte[] generateSalt() {
        byte[] salt = new byte[16];// salt를 저장할 때 바이트 배열을 생성(16바이트 -> 128비트)
        SecureRandom random = new SecureRandom();// SecureRandom객체 생성하여 난수 생성기(암호화용)
        random.nextBytes(salt); // 난수로 salt 배열 채우기 salt[i]
        return salt; // 반환
    }

    // 비밀번호에 소금 뿌리기 단계
    public byte[] encryptRaw(String password, byte[] salt) {
        try {
            // password랑 salt를 섞어서 ITERATIONS번 섞고 최종적으로 KEY_LENGTH길이의 결과를 만들어라.
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

            // SecretKeyFactory : 암호화 키를 만들어주는 공장
            // PBKDF2 With HmacSHA1: 어떤 방식으로 키를 만들지 지정함
            // PBKDF2: 비밀번호 기반 키 생성 함수
            // HmacSHA1: 해시 알고리즘을 사용해서 안전하게 키 생성

            // PBKDF2 + SHA1 방식으로 안전한 키를 만들어주는 도구(factory)를 준비 단계

            // 문제점
            // PBKDF2 자체는 널리 사용되고 있고 RFC 문서에도 권장 구조로 남아있음
            // SHA-1는 오래된 해시 함수, 충돌 공격(서로 다른 두 입력값이 같은 해시값을 만들어내는 상황을 인위적으로 찾아내는 공격) 취약함

            SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);

            // 바이트 배열을 문자열로 변환 DB에 저장하거나 JSON으로 전송할 때 편리함
            return factory.generateSecret(spec).getEncoded();

            // NoSuchAlgorithmException: 요청한 암호화 알고리즘(PBKDF2WithHmacSHA1)이 없을 경우
            // InvalidKeySpecException: 키 만들 때 spec이 잘못됐을 때
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            // 개발자가 환경변수에 알고리즘 이름을 잘못 썼을 때 "이런 알고리즘 없어!"하고 예외처리
            throw new RuntimeException(e);
        }
    }

    // hash를 Base64 문자열로 반환(저장/전송용)
    public String encrypt(String password, byte[] salt) {
        return Base64.getEncoder().encodeToString(encryptRaw(password, salt)); // 비밀번호 + salt : 단방향 해시
    }

    // 로그인 검증 시 사용: 입력 비번 + DB salt(Base64) hash 후 DB hash(Base64)와 비교
    public boolean matches(String inputPassword, String dbSaltBase64, String dbPasswordBase64) {
        byte[] saltBytes = Base64.getDecoder().decode(dbSaltBase64);
        String hashed = encrypt(inputPassword, saltBytes);
        return hashed.equals(dbPasswordBase64);
    }

    // 유틸 : salt바이트 -> Base64, Base64->바이트
    public String toBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public byte[] fromBase64(String base64) {
        return Base64.getDecoder().decode(base64);
    }
}
