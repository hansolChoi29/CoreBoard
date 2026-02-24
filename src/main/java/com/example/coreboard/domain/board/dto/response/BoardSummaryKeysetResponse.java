package com.example.coreboard.domain.board.dto.response;

import java.time.LocalDateTime;

public record BoardSummaryKeysetResponse(
        Long id,
        Long userId,
        String title,
        LocalDateTime createdDate) {
}
