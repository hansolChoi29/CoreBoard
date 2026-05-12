package com.example.coreboard.domain.post.dto.command;


import com.example.coreboard.domain.common.type.ContentFormat;

import java.util.List;

public record UpdatePostCommand(
        Long id,
        String username,
        String title,
        String content,
        ContentFormat contentFormat,
        List<Long> keepAttachmentIds,
        List<Long> newAttachmentIds
) {
}
