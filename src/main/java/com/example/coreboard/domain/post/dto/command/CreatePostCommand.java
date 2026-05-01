package com.example.coreboard.domain.post.dto.command;

import com.example.coreboard.domain.post.entity.ContentFormat;

public record CreatePostCommand(
        Long boardId,
        String title,
        String content,
        ContentFormat contentFormat
) {
}
