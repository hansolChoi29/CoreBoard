package com.example.coreboard.domain.comment.dto.response;

import com.example.coreboard.domain.comment.entity.Comment;

import java.time.LocalDateTime;

public record GetAllCommentResponse(
        Long commentId,
        String content,
        String nickname,
        LocalDateTime createdDate
) {

    public static GetAllCommentResponse from(Comment comment){
        return new GetAllCommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getUsername(),
                comment.getCreatedDate()
        );
    }
}
