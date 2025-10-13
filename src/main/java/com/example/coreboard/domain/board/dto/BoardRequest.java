package com.example.coreboard.domain.board.dto;

import static com.example.coreboard.domain.common.exception.board.BoardErrorCode.*;

import com.example.coreboard.domain.common.exception.board.BoardErrorException;

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

    // 글자 수 및 빈값 예외처리
    public void validation() {
        String title = boardTitle == null ? null : boardTitle.trim();
        String contents = boardContents == null ? null : boardContents.trim();

        boolean titleBlank = title == null || title.isBlank();
        boolean contentsBlank = contents == null || contents.isBlank();

        if (titleBlank && contentsBlank) {
            throw new BoardErrorException(TITLE_AND_CONTENTS_BLANK);
        }
        if (titleBlank) {
            throw new BoardErrorException(NOT_TITLE);
        }
        if (contentsBlank) {
            throw new BoardErrorException(NOT_CONTENT);
        }
        if (title.length() > 255) {
            throw new BoardErrorException(TITLE_TOO_LONG);
        }
        if (contents.length() > 1000) {
            throw new BoardErrorException(CONTENT_TOO_LONG);
        }

        this.boardTitle = title;
        this.boardContents = contents;
    }

    public String getBoardTitle() {
        return boardTitle;
    }

    public String getBoardContents() {
        return boardContents;
    }
}
