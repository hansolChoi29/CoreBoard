package com.example.coreboard.domain.post.dto;


import java.time.LocalDateTime;

public record UpdatePostDto(
        Long id,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
