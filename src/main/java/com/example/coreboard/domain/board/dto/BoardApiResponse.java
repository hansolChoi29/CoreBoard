package com.example.coreboard.domain.board.dto;

public class BoardApiResponse {
    private String boardTitle;
    private String message;

    public BoardApiResponse(
            String boardTitle,
            String message
    ) {
        this.boardTitle = boardTitle;
        this.message = message;
    }


    public String getBoardTitle() {
        return boardTitle;
    }

    public String getMessage() {
        return message;
    }
}
