package com.example.coreboard.domain.admin.dto.response;

import com.example.coreboard.domain.users.entity.UserRole;

public record AdminPatchResponse(
        Long id,
        String username,
        UserRole role
) {
}
