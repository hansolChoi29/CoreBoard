package com.example.coreboard.domain.auth.dto.response;

import jakarta.validation.constraints.NotBlank;

public record TokenResponse(
        @NotBlank String accessToken) {
}