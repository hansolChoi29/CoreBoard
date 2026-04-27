package com.example.coreboard.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PostUpdateRequest(
        @NotBlank String title,
        @NotBlank String content) {
}
