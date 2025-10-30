package com.example.coreboard.domain.common.config;


import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class PasswordManager {
    // BCrypt는 salt가 자동 포함

    private static final int COST = 12; // 2의 12승만큼 반복 조합

    // 비밀번호를 bcrypt로 해시하는 전 과정
    public String encrypt(String password) {
        // BCrypt : 암호화 도구
        // 사용 이유 : bcrypt가 PBKDF2보다 비밀번호 해시에 더 적합하고 무차별 대입 공격에 강함
        // withDefaults : 기본 설정
        // hashToString : 비밀번호 해시해서 문자열로 변환
        return BCrypt.withDefaults().hashToString(COST, password.toCharArray()); // 2의 12승 만큼 조합하여 비밀번호를 문자 배열로 변환하겠다.
    }

    // 로그인 시 비밀번호 검증
    public boolean matches(
            String inputPassword,
            String storedHash
    ) {
        // storedHash가 isEmpty거나 null이면 false = public final boolean verified;
        if (storedHash == null || storedHash.isEmpty())
            return false;
        return BCrypt.verifyer()
                // verify(입력 비밀번호, 저장된 해시) : 입력된 비밀번호를 bcrypt 규칙대로 다시 해시하여 DB의 해시와 비교
                .verify(inputPassword.toCharArray(), storedHash)
                .verified; // verified : 두 해시가 일치하면 true, 다르면 false
    }
}
