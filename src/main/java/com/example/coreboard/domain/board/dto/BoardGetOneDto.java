package com.example.coreboard.domain.board.dto;

import java.time.LocalDateTime;

public class BoardGetOneDto {
    private final Long id;
    private final long userId;
    private final String title;
    private final String content;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastModifiedDate;

    public BoardGetOneDto(
            Long id,
            long userId,
            String title,
            String content,
            LocalDateTime createdDate,
            LocalDateTime lastModifiedDate
    ) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    public Long getId() {
        return id;
    }

    public long getUserId() {
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

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

}
