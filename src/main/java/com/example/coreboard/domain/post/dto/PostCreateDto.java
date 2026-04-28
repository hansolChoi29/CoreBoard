package com.example.coreboard.domain.post.dto;

import java.time.LocalDateTime;

public class PostCreateDto {
    private final Long id;
    private final long userId;
    private final String title;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updateAt;

    public PostCreateDto(
            Long id,
            long userId,
            String title,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updateAt
    ) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updateAt = updateAt;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public long getUserId() {
        return userId;
    }
}
