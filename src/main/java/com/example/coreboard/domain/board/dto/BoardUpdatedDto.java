package com.example.coreboard.domain.board.dto;


public class BoardUpdatedDto {
    // 서비스 전용 dto
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
