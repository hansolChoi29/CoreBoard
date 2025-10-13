package com.example.coreboard.domain.board.dto;


public class BoardRequest {
    // 요청 넣어야 하는 것
    private String boardTitle;
    private String boardContents;

    public BoardRequest(
            String boardTitle,
            String boardContents
    ) {
        this.boardTitle = boardTitle;
        this.boardContents = boardContents;

    }
    public String getBoardTitle() {
        return boardTitle;
    }

    public String getBoardContents() {
        return boardContents;
    }


}
