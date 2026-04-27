package com.example.coreboard.domain.post.dto.response;

import java.time.LocalDateTime;

public record PostCreateResponse(
        Long id,
        Long userId,
        String title,
        String content,
        LocalDateTime createdDate) {
}
