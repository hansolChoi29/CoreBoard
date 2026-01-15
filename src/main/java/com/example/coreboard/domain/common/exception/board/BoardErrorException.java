package com.example.coreboard.domain.common.exception.board;

import com.example.coreboard.domain.common.exception.ErrorException;

public class BoardErrorException extends ErrorException {

    public BoardErrorException(BoardErrorCode boardErrorCode) {
        super(boardErrorCode.getStatus(), boardErrorCode.getMessage());
    }
}
