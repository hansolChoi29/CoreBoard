package com.example.coreboard.domain.board.dto;


public class BoardUpdatedDto {
    private final Long id;

    public BoardUpdatedDto(
            Long id
    ) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
