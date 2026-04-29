package com.example.coreboard.domain.auth.dto.command;


public record SignUpCommand(
         String username,
         String nickname,
         String password,
         String confirmPassword,
         String email,
         String phoneNumber

) {
    // TODO : 비밀번호 일치 여부 검증 필요
}
