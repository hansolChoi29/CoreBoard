package com.example.coreboard.domain.board.dto;

import java.time.LocalDateTime;

public class BoardGetAllResponse {
    String boardTitle;
    String boardContents;
    LocalDateTime createdDate;
    LocalDateTime lastModifiedDate;

    public BoardGetAllResponse(
            String boardTitle,
            String boardContents,
            LocalDateTime createdDate,
            LocalDateTime lastModifiedDate
    ) {
        this.boardTitle = boardTitle;
        this.boardContents = boardContents;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getBoardTitle() {
        return boardTitle;
    }

    public String getBoardContents() {
        return boardContents;
    }

    public LocalDateTime getCreatedDate(){
        return createdDate;
    }

    public LocalDateTime getLastModifiedDate(){
        return lastModifiedDate;
    }
}
