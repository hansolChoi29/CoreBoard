package com.example.coreboard.domain.board.dto.command;

public record DeleteBoardCommand(
        Long id,
        String username
) {
}
