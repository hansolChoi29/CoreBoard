package com.example.coreboard.domain.common.exception.comment;

import com.example.coreboard.domain.common.exception.ErrorException;

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
