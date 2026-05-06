package com.example.coreboard.domain.comment.dto.query;

public record GetCommentQuery(
        Long postId,
        int page,
        int size
) {
}
