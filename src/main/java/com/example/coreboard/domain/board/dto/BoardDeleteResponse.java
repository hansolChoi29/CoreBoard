package com.example.coreboard.domain.board.dto;

public class BoardDeleteResponse {
    // 응답으로 나와야 하는 것
    Long id;
    String boardTitle;
    String boardContents;


    public BoardDeleteResponse(Long id, String boardTitle, String boardContents) {
        this.id = id;
        this.boardTitle = boardTitle;
        this.boardContents = boardContents;
    }

    public String getBoardTitle() {
        return boardTitle;
    }

    public String getBoardContents() {
        return boardContents;
    }

    public Long getId() {
        return id;
    }
}
