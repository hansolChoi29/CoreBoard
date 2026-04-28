package com.example.coreboard.domain.post.dto;


import java.time.LocalDateTime;

public class PostUpdatedDto {
    private final Long id;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public PostUpdatedDto(
            Long id,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getId() {
        return id;
    }
}
