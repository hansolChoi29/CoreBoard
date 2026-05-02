package com.example.coreboard.domain.board.dto.request;


public record UpdateBoardRequest(
        Long id,
        String name,
        String slug,
        boolean answerAcceptedEnabled,
        boolean commentEnabled,
        boolean requireAttachment,
        int maxAttachmentCount,
        int maxContentLength
) {
}
