package com.example.coreboard.domain.board.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BoardUpdateRequest(
        @NotBlank String title,
        @NotBlank String content) {
}
