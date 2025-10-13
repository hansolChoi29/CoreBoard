package com.example.coreboard.domain.board.dto;

public class BoardUpdateResponse {
    String boardTitle;
    String boardContents;

    public BoardUpdateResponse(String boardTitle, String boardContents) {
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
