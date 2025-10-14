package com.example.coreboard.domain.board.dto;

import com.example.coreboard.domain.board.entity.Board;

public class BoardDeleteResponse {
    private final Long id;
    private final long userId;
    private final String boardTitle;


    public BoardDeleteResponse(Board board) {
        this.id = board.getId();
        this.userId = board.getUserId();
        this.boardTitle = board.getBoardTitle();
    }

    public String getBoardTitle() {
        return boardTitle;
    }

    public Long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }
}
