package com.example.coreboard.domain.auth.dto;

public class AuthSignInDto {
    private final String accessToken;
    private final String refreshToken;

    public AuthSignInDto(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
}
