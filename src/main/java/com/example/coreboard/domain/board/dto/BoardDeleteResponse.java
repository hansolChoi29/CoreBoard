package com.example.coreboard.domain.board.dto;

import com.example.coreboard.domain.board.entity.Board;

public class BoardDeleteResponse {
    private final Long id;



    public BoardDeleteResponse(Board board) {
        this.id = board.getId();

    }
    public Long getId() {
        return id;
    }

}
