package com.example.coreboard.domain.post.dto.response;

import com.example.coreboard.domain.comment.dto.response.GetAllCommentResponse;
import com.example.coreboard.domain.common.response.SliceResponse;

import java.time.LocalDateTime;
import java.util.List;

public record GetOnePostResponse(
        Long id,
        Long userId,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updateAt,
        SliceResponse<GetAllCommentResponse> comments,
        List<PostAttachmentResponse> attachments
) {
}
