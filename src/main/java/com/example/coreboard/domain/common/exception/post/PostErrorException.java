package com.example.coreboard.domain.common.exception.post;

import com.example.coreboard.domain.common.exception.ErrorException;

public class PostErrorException extends ErrorException {
    public PostErrorException(PostErrorCode boardErrorCode) {
        super(
                boardErrorCode.getStatus(),
                boardErrorCode.getCode(),
                boardErrorCode.getMessage(),
                boardErrorCode.getErrors()
        );
    }
}
