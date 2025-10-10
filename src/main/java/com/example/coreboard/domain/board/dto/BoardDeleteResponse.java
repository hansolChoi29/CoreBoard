package com.example.coreboard.domain.board.dto;

public class BoardDeleteResponse {
    // 응답으로 나와야 하는 것
    Long boardId;
    String boardTitle;
    String boardContents;


    public BoardDeleteResponse(Long boardId, String boardTitle, String boardContents) {
        this.boardId = boardId;
        this.boardTitle = boardTitle;
        this.boardContents = boardContents;
    }

    public String getBoardTitle() {
        return boardTitle;
    }

    public String getBoardContents() {
        return boardContents;
    }

    public Long getBoardId() {
        return boardId;
    }
}
