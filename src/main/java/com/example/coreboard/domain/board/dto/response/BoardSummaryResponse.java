package com.example.coreboard.domain.board.dto.response;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;

public record BoardSummaryResponse(
        Long id,
        Long userId,
        @NotBlank String title,
        LocalDateTime createdDate) {
}