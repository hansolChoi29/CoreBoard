package com.example.coreboard.domain.admin.dto;

import com.example.coreboard.domain.users.entity.UserRole;

public record AdminPatchDto(
        Long id,
        UserRole role,
        String username
) {
}
