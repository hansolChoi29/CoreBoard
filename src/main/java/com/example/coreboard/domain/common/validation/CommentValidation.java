package com.example.coreboard.domain.common.validation;

import com.example.coreboard.domain.comment.dto.request.CommentRequest;
import com.example.coreboard.domain.common.exception.comment.CommentErrorCode;
import com.example.coreboard.domain.common.exception.comment.CommentErrorException;

public class CommentValidation {
    public static void validate(CommentRequest request) {
        if (
                request == null
                        || request.content() == null
                        || request.content().isBlank()) {
            throw new CommentErrorException(CommentErrorCode.COMMENT_CONTENT_IS_BLANK);
        }
    }
}
