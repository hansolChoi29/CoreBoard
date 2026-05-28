package com.example.coreboard.domain.board.exception;

import com.example.coreboard.global.exception.ErrorException;

public class BoardErrorException extends ErrorException {
    public BoardErrorException(BoardErrorCode boardErrorCode) {
        super(
                boardErrorCode.getStatus(),
                boardErrorCode.getCode(),
                boardErrorCode.getMessage(),
                boardErrorCode.getErrors()
        );
    }
}
