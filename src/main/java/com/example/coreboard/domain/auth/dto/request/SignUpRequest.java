package com.example.coreboard.domain.auth.dto.request;

import com.example.coreboard.domain.users.entity.UserRole;

public record SignUpRequest(
        String username,
        String nickname,
        String password,
        String confirmPassword,
        String email,
        String phoneNumber,
        UserRole role
) {
}
