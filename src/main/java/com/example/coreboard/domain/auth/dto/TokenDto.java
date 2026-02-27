package com.example.coreboard.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenDto(
        @NotBlank String accessToken,
        @NotBlank String refreshToken) {
}
