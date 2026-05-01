package com.example.coreboard.domain.post.dto.request;

import com.example.coreboard.domain.post.entity.ContentFormat;

public record CreatePostRequest(
        Long boardId,
        String title,
        String content,
        ContentFormat contentFormat) {
}
