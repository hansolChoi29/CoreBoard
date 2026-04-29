package com.example.coreboard.domain.auth.dto;


public record TokenDto(
        String accessToken,
        String refreshToken
) {
}
