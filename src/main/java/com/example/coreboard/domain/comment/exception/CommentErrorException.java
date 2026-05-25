package com.example.coreboard.domain.comment.exception;

import com.example.coreboard.global.exception.ErrorException;

public class CommentErrorException extends ErrorException {
    public CommentErrorException(CommentErrorCode commentErrorCode) {
        super(
                commentErrorCode.getStatus(),
                commentErrorCode.getCode(),
                commentErrorCode.getMessage(),
                commentErrorCode.getErrors()
        );
    }
}
