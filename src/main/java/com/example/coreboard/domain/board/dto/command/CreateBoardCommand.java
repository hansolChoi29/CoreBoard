package com.example.coreboard.domain.board.dto.command;

import com.example.coreboard.domain.users.entity.UserRole;

public record CreateBoardCommand (
        String name,
        String slug,
        boolean answerAcceptedEnabled,
        boolean commentEnabled,
        boolean requireAttachment,
        int maxAttachmentCount,
        int maxContentLength,
        UserRole requiredWriteRole
){
}
