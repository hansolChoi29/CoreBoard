package com.example.coreboard.domain.admin.dto.response;

import com.example.coreboard.domain.users.entity.UserRole;
import jakarta.validation.constraints.NotBlank;

public record AdminGetResponse(
        @NotBlank Long userId,
        @NotBlank String username,
        @NotBlank UserRole role
) {
}
