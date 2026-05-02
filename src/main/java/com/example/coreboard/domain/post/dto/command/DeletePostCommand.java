package com.example.coreboard.domain.post.dto.command;

public record DeletePostCommand(
        Long id,
        String username
) {
}
