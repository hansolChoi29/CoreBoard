package com.example.coreboard.domain.post.dto.response;

import java.time.LocalDateTime;

public record PostSummaryKeysetResponse(
        Long id,
        Long userId,
        String title,
        LocalDateTime createdDate) {
}
