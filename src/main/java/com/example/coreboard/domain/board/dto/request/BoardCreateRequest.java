package com.example.coreboard.domain.board.dto.request;

public class BoardCreateRequest {
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
