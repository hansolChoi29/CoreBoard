package com.example.coreboard.domain.board.dto;

import com.example.coreboard.domain.board.entity.Board;

import java.time.LocalDateTime;

public class BoardGetAllResponse {
    private final Long id;
    private final String username;
    private final String boardTitle;
    private final String boardContents;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastModifiedDate;

    public BoardGetAllResponse(Board board) {
        this.id=board.getId();
        this.username=board.getUsername();
        this.boardTitle = board.getBoardTitle();
        this.boardContents = board.getBoardContents();
        this.createdDate = board.getCreatedDate();
        this.lastModifiedDate = board.getLastModifiedDate();
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
