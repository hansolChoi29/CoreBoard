package com.example.coreboard.domain.auth.dto.request;

import com.example.coreboard.domain.users.entity.UserRole;
import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(
        @NotBlank String username,
        @NotBlank String nickname,
        @NotBlank String password,
        @NotBlank String confirmPassword,
        @NotBlank String email,
        @NotBlank String phoneNumber,
        @NotBlank UserRole role
        ) {
}
