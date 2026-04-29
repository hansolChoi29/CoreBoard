package com.example.coreboard.domain.auth.dto.response;

import com.example.coreboard.domain.users.entity.UserRole;

public record SignUpResponse(
        String username,
        UserRole role
) {
}
