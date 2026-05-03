package com.example.coreboard.domain.board.dto.response;

import com.example.coreboard.domain.post.dto.response.PostSummaryResponse;
import com.example.coreboard.domain.users.entity.UserRole;

import java.util.List;

public record GetOneBoardResponse(
        Long id,
        String name,
        String slug,
        boolean answerAcceptedEnabled,
        boolean commentEnabled,
        boolean requireAttachment,
        int maxAttachmentCount,
        UserRole allowedWriteRoles,
        List<PostSummaryResponse> posts
) {
}
