package com.example.coreboard.domain.board.dto;

import com.example.coreboard.domain.board.entity.Board;

public class BoardDeleteResponse {
    private final Long id;
    private final String username;
    private final String boardTitle;


    public BoardDeleteResponse(Board board) {
        this.id = board.getId();
        this.username=board.getUsername();
        this.boardTitle = board.getBoardTitle();

    }

    public String getBoardTitle() {
        return boardTitle;
    }

    public Long getId() {
        return id;
    }

    public String getUsername(){
        return username;
    }
}
