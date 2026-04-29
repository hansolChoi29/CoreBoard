package com.example.coreboard.domain.admin.dto.query;

import com.example.coreboard.domain.users.entity.UserRole;
import org.springframework.data.domain.Pageable;

public record AdminUserListQuery(
        UserRole role,
        Pageable pageable,
        String username
) {
}
