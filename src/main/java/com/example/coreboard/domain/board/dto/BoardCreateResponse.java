package com.example.coreboard.domain.board.dto;


import java.time.LocalDateTime;

public class BoardCreateResponse {
    // private final : 값을 한 번만 저장하고 절대 바꾸지 않겠다.
    private final Long id;
    private final long userId;
    private final String boardTitle;
    private final String boardContents;
    private final LocalDateTime createdDate;

    public BoardCreateResponse(
            Long id,
            long userId,
            String boardTitle,
            String boardContents,
            LocalDateTime createdDate
    ) {
        this.id = id;
        this.userId = userId;
        this.boardTitle = boardTitle;
        this.boardContents = boardContents;
        this.createdDate = createdDate;
    }

    public Long getId() {
        return id;
    }

    public String getBoardTitle() {
        return boardTitle;
    }

    public String getBoardContents() {
        return boardContents;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public long getUserId() {
        return userId;
    }

}
