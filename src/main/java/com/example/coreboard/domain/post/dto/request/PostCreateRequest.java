package com.example.coreboard.domain.post.dto.request;

import com.example.coreboard.domain.post.entity.ContentFormat;
import jakarta.validation.constraints.NotBlank;

public record PostCreateRequest(
        @NotBlank Long boardId,
        @NotBlank String title,
        @NotBlank String content,
        @NotBlank ContentFormat contentFormat) {
}
