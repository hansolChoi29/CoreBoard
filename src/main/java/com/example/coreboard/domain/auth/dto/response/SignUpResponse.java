package com.example.coreboard.domain.auth.dto.response;

import com.example.coreboard.domain.users.entity.UserRole;
import jakarta.validation.constraints.NotBlank;

public record SignUpResponse(
        @NotBlank String username,
        @NotBlank UserRole role
        ) {
}
