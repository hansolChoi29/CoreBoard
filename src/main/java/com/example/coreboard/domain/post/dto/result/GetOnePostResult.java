package com.example.coreboard.domain.post.dto.result;

import java.time.LocalDateTime;

public record GetOnePostResult(
        Long id,
        long userId,
        String title,
        String content,
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate
) {
}
