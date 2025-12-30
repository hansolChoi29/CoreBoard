package com.example.coreboard.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String confirmPassword,
        @NotBlank String email,
        @NotBlank String phoneNumber) {
}
