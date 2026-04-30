package com.example.coreboard.domain.board.dto.request;

import com.example.coreboard.domain.users.entity.UserRole;

public record CreateBoardRequest(
        String name,
        String slug,
        boolean answerAcceptedEnabled,
        boolean commentEnabled,
        boolean requireAttachment,
        int maxAttachmentCount,
        int maxContentLength,
        UserRole requiredWriteRole
) {
}
