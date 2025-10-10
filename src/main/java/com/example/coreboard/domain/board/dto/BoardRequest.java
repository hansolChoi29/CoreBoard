package com.example.coreboard.domain.board.dto;

import com.example.coreboard.domain.board.entity.Board;

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

    public static Board createBoard(
            String boardTitle,
            String boardContents
    ) {
        return new Board(boardTitle, boardContents);
    }


    public String getBoardTitle() {
        return boardTitle;
    }

    public String getBoardContents() {
        return boardContents;
    }
}
