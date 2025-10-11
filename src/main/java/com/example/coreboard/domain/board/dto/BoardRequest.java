package com.example.coreboard.domain.board.dto;


import java.time.LocalDateTime;

public class BoardRequest {
    // 요청 넣어야 하는 것
    private String boardTitle;
    private String boardContents;
    private LocalDateTime lastModifiedDate;

    public BoardRequest(
            String boardTitle,
            String boardContents,
            LocalDateTime lastModifiedDate
    ) {
        this.boardTitle = boardTitle;
        this.boardContents = boardContents;
        this.lastModifiedDate=lastModifiedDate;
    }

    public String getBoardTitle() {
        return boardTitle;
    }

    public String getBoardContents() {
        return boardContents;
    }
    public  LocalDateTime getLastModifiedDate(){
        return lastModifiedDate;
    }
}
