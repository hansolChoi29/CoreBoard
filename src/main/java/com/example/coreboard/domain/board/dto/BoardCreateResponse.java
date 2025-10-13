package com.example.coreboard.domain.board.dto;

import com.example.coreboard.domain.board.entity.Board;

import java.time.LocalDateTime;

public class BoardCreateResponse {
    String boardTitle;
    String boardContents;


    public BoardCreateResponse(
            String boardTitle,
            String boardContents

    ) {
        this.boardTitle = boardTitle;
        this.boardContents = boardContents;

    }

    public BoardCreateResponse(Board board) {
    }

    public String getBoardTitle() {
        return boardTitle;
    }

    public String getBoardContents() {
        return boardContents;
    }
}
