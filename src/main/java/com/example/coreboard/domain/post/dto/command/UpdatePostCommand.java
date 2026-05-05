package com.example.coreboard.domain.post.dto.command;


import com.example.coreboard.domain.post.entity.ContentFormat;

public record UpdatePostCommand(
        Long id,
        String username,
        String title,
        String content,
        ContentFormat contentFormat
) {
}
