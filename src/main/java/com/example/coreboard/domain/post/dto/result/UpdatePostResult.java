package com.example.coreboard.domain.post.dto.result;


import java.time.LocalDateTime;

public record UpdatePostResult(
        Long id,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
