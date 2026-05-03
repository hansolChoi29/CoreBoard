package com.example.coreboard.domain.board.dto.command;



public record UpdateBoardCommand(
        Long id,
        String name,
        String slug,
        boolean answerAcceptedEnabled,
        boolean commentEnabled,
        boolean requireAttachment,
        int maxAttachmentCount
) {
}
