package com.example.coreboard.domain.post.dto.request;

import com.example.coreboard.domain.post.entity.ContentFormat;

import java.util.List;

public record UpdatePostRequest(
        String title,
        String content,
        ContentFormat contentFormat,
        List<Long> keepAttachmentIds,
        List<Long> newAttachmentIds
) {
}
