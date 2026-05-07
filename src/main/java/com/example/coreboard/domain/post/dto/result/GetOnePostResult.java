package com.example.coreboard.domain.post.dto.result;

import com.example.coreboard.domain.comment.dto.response.GetAllCommentResponse;
import com.example.coreboard.domain.common.response.SliceResponse;

import java.time.LocalDateTime;

public record GetOnePostResult(
        Long id,
        long userId,
        String title,
        String content,
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate,
        SliceResponse<GetAllCommentResponse> comments
) {
}
