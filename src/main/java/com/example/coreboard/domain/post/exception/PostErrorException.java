package com.example.coreboard.domain.post.exception;

import com.example.coreboard.global.exception.ErrorException;

public class PostErrorException extends ErrorException {
    public PostErrorException(PostErrorCode postErrorCode) {
        super(
                postErrorCode.getStatus(),
                postErrorCode.getCode(),
                postErrorCode.getMessage(),
                postErrorCode.getErrors()
        );
    }
}
