package com.example.coreboard.domain.auth.dto;

public class TokenResponse {
    // 로그인 할 때 필요해서 응답
     String accessToken;
    String refreshToken;

    public TokenResponse(String accessToken,String refreshToken){
        this.accessToken=accessToken;
        this.refreshToken=refreshToken;
    }
}
