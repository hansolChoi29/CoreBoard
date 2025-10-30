package com.example.coreboard.domain.board.dto;

import java.time.LocalDateTime;

public class BoardCreateDto {
    private final Long id;
    private final long userId;
    private final String title;
    private final String content;
    private final LocalDateTime createdDate;

    public BoardCreateDto(
            Long id,
            long userId,
            String title,
            String content,
            LocalDateTime createdDate
    ) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.createdDate = createdDate;
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

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public long getUserId() {
        return userId;
    }
}
