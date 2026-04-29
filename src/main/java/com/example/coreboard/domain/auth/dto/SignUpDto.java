package com.example.coreboard.domain.auth.dto;

import com.example.coreboard.domain.users.entity.UserRole;

public record SignUpDto(
        String username,
        UserRole role
) {
}