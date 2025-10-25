package com.example.coreboard.domain.board.dto;

public class BoardUpdateRequest {
    // 기존 데이터라는 점에서 생성 요청 DTO와 별도로 분리하여 사용해야 함

    private String title;
    private String content;

    public BoardUpdateRequest(
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
