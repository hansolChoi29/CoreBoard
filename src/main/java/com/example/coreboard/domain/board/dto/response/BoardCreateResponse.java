package com.example.coreboard.domain.board.dto.response;

import java.time.LocalDateTime;

public record BoardCreateResponse(
        Long id,
        Long userId,
        String title,
        String content,
        LocalDateTime createdDate) {
}
