package com.example.coreboard.domain.post.dto.command;

import com.example.coreboard.domain.post.entity.ContentFormat;

public record PostCreateCommand(
        Long boardId,
        String title,
        String content,
        ContentFormat contentFormat
) {
}
