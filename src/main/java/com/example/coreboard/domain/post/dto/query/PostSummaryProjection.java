package com.example.coreboard.domain.post.dto.query;

import java.time.LocalDateTime;

public interface PostSummaryProjection {
    Long getId();

    String getTitle();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    String getWriterName();
}
