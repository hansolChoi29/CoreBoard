package com.example.coreboard.domain.common.exception.post;

import com.example.coreboard.domain.common.exception.ErrorException;

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
