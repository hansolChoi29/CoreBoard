package com.example.coreboard.domain.post.dto.result;

import com.example.coreboard.domain.comment.dto.response.GetAllCommentResponse;
import com.example.coreboard.domain.common.response.SliceResponse;
import com.example.coreboard.domain.post.dto.response.PostAttachmentResponse;

import java.time.LocalDateTime;
import java.util.List;

public record GetOnePostResult(
        Long id,
        long userId,
        String title,
        String content,
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate,
        SliceResponse<GetAllCommentResponse> comments,
        List<PostAttachmentResponse> attachments
) {
}
