package com.example.coreboard.domain.common.config;


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

    public String encrypt(String password) {
        try {
            //랜덤하게 넣겠다.(해시 만들기 위한 준비 단계)
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);



            // PBEKeySpec : 비밀번호를 안전하게 변환하기 위한 규칙을 정의하는 객체임
            // toCharArray : 사용자가 입력한 비밀번호 문자열을 문자 배열로 바꾼 것
            // 85319 : 반복횟수(반복이 많아질수록 안전하지만 느려짐)
            // 128 : key length

            //비밀번호 문자열 + 랜덤 salt → 8만 번 반복 → 128비트 길이의 안전한 해시 생성
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 85319, 128); //128비트



            // SecretKeyFactory : 암호화 키를 만들어주는 공장
            //PBKDF2 With HmacSHA1: 어떤 방식으로 키를 만들지 지정함
                // PBKDF2: 비밀번호 기반 키 생성 함수
                // HmacSHA1: 해시 알고리즘을 사용해서 안전하게 키 생성


            // PBKDF2 + SHA1 방식으로 안전한 키를 만들어주는 도구(factory)를 준비 단계
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");


            //generateSecret(spec): 방금 만든 팩토리로 실제 비번 해시 계산
            // 최종적으로 DB에 저장할 수 있는 형태임
            byte[] hash = factory.generateSecret(spec).getEncoded();


            // 바이트 배열을 문자열로 변환 DB에 저장하거나 JSON으로 전송할 때 편리함
            return Base64.getEncoder().encodeToString(hash);


            // NoSuchAlgorithmException: 요청한 암호화 알고리즘(PBKDF2WithHmacSHA1)이 없을 경우
            // InvalidKeySpecException: 키 만들 때 spec이 잘못됐을 때
            
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}
