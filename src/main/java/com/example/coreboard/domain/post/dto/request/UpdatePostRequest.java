package com.example.coreboard.domain.post.dto.request;

import com.example.coreboard.domain.post.entity.ContentFormat;

public record UpdatePostRequest(
        String title,
        String content,
        ContentFormat contentFormat
) {
}
