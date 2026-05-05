package com.example.coreboard.domain.board.dto.result;

public record GetBoardListResult(
        Long boardId,
        String name,
        String slug
) {
}
