package com.example.coreboard.domain.board.dto;


public class BoardResponse {
    // 응답으로 나와야 하는 것
    String boardTitle;
    String boardContents;

    public BoardResponse(String boardTitle, String boardContents) {
        this.boardTitle = boardTitle;
        this.boardContents = boardContents;
    }

    public String getBoardTitle(){
        return boardTitle;
    }
    public String getBoardContents(){
        return boardContents;
    }
}
