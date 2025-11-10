package com.example.coreboard.domain.board.dto.response;


public class BoardUpdateResponse {
    private final Long id;

    public BoardUpdateResponse(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
