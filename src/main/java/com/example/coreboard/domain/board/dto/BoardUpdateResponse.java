package com.example.coreboard.domain.board.dto;

import java.time.LocalDateTime;

public class BoardUpdateResponse {
    private final Long id;
    private final long userId;
    private final String title;
    private final String contents;
    private final LocalDateTime lastModifiedDate;

    public BoardUpdateResponse(
            Long id,
            long userId,
            String title,
            String contents,
            LocalDateTime lastModifiedDate
    ) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.contents = contents;
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

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }
}
