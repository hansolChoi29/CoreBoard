package com.example.coreboard.domain.post.dto.response;

public record  PostAttachmentResponse(
        Long id,
        String originalFileName,
        String storeUrl,
        String contentType,
        Long fileSize
) {
}
