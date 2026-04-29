package com.example.coreboard.domain.admin.dto.command;

import com.example.coreboard.domain.users.entity.UserRole;

public record AdminPatchCommand(
        Long id,
        UserRole role,
        String username
) {
}
