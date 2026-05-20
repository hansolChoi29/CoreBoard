package com.example.coreboard.domain.post.dto.response;

import java.time.LocalDateTime;

public record PostSummaryResponse(
        Long id,
        String getWriterName,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
