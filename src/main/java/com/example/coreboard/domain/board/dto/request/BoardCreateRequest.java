package com.example.coreboard.domain.board.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BoardCreateRequest(
        @NotBlank String title,
        @NotBlank String content) {
}
