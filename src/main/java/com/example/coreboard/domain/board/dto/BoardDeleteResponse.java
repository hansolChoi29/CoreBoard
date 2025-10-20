package com.example.coreboard.domain.board.dto;


public class BoardDeleteResponse {
    private final Long id;



    public BoardDeleteResponse(Long id) {
        this.id = id;
    }
    public Long getId() {
        return id;
    }

}
