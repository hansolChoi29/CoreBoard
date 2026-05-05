package com.example.coreboard.domain.post.dto.response;

import java.time.LocalDateTime;

public record UpdatePostResponse(
        Long id,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
