package com.example.coreboard.domain.board.dto.response;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;

public record BoardGetOneResponse(
        Long id,
        Long userId,
        @NotBlank String title,
        @NotBlank String content,
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate) {
}
