package com.example.coreboard.domain.post.dto;

import java.time.LocalDateTime;

public record PostGetOneDto(
        Long id,
        long userId,
        String title,
        String content,
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate
) {
}
