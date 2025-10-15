package com.example.coreboard.domain.board.dto;


import java.time.LocalDateTime;

public class BoardGetOneResponse {
    private final Long id;
    private final long userId;
    private final String title;
    private final String contents;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastModifiedDate;


    public BoardGetOneResponse(
            Long id,
            long userId,
            String title,
            String contents,
            LocalDateTime createdDate,
            LocalDateTime lastModifiedDate
    ) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.contents = contents;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    public Long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getBoardTitle() {
        return title;
    }

    public String getBoardContents() {
        return contents;
    }

    public LocalDateTime getCreateDate() {
        return createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }
}
