package com.example.coreboard.domain.post.dto.response;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;

public record PostGetOneResponse(
        Long id,
        Long userId,
        @NotBlank String title,
        @NotBlank String content,
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate) {
}
