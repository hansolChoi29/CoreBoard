package com.example.coreboard.domain.admin.dto.response;

import com.example.coreboard.domain.users.entity.UserRole;

public record AdminGetResponse(
        Long userId,
        String username,
        UserRole role
) {
}
