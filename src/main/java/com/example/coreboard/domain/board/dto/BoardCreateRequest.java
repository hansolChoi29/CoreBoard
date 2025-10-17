package com.example.coreboard.domain.board.dto;

public class BoardCreateRequest {
    // 생성요청 DTO는 Board의 Id가 필요없어서 수정요청 DTO와 별도로 사용되어야 함

    // 요청 넣어야 하는 것
    private String title;
    private String content;

    public BoardCreateRequest(
            String title,
            String content
    ) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
