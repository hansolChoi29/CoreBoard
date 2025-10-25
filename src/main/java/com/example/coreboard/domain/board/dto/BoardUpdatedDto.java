package com.example.coreboard.domain.board.dto;


public class BoardUpdatedDto {
    // 서비스 전용 dto
    // 또한, 서비스용 응답용(resultDto)도 변경될 일 없기 때문에 final 키워드를 사용한다.

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
