package com.example.coreboard.domain.board.dto.response;

public record GetBoardListResponse(
        Long boardId,
        String name,
        String slug
) {
}
