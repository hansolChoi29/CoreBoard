package com.example.coreboard.domain.board.dto;

import com.example.coreboard.domain.post.dto.response.PostSummaryResponse;
import com.example.coreboard.domain.users.entity.UserRole;

import java.util.List;

public record GetOneBoardDto(
        Long id,
        String name,
        String slug,
        boolean answerAcceptedEnabled,
        boolean commentEnabled,
        boolean requireAttachment,
        int maxAttachmentCount,
        int maxContentLength,
        UserRole requiredWriteRole,
        List<PostSummaryResponse> posts
) {
}
