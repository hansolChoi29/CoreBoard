package com.example.coreboard.domain.post.dto.command;

import com.example.coreboard.global.type.ContentFormat;

import java.util.List;

public record CreatePostCommand(
        Long boardId,
        String title,
        String content,
        ContentFormat contentFormat,
        List<Long> attachmentIds
) {
}
