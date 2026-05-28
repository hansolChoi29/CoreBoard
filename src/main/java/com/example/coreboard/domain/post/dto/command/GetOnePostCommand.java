package com.example.coreboard.domain.post.dto.command;

public record GetOnePostCommand(
        Long id,
        String viewerKey
) {
}
