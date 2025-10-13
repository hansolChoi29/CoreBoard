package com.example.coreboard.domain.board.dto;

import java.time.LocalDateTime;

public class BoardUpdateResponse {
    private final Long id;
    private final long userId;
    private final String boardTitle;
    private final String boardContents;
    private final LocalDateTime lastModifiedDate;

    public BoardUpdateResponse(
            Long id,
            long userId,
            String boardTitle,
            String boardContents,
            LocalDateTime lastModifiedDate
    ) {
        this.id = id;
        this.userId = userId;
        this.boardTitle = boardTitle;
        this.boardContents = boardContents;
        this.lastModifiedDate = lastModifiedDate;
    }

    public Long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getBoardTitle() {
        return boardTitle;
    }

    public String getBoardContents() {
        return boardContents;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }
}
