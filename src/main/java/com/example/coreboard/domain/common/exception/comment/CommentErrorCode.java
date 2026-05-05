package com.example.coreboard.domain.common.exception.comment;

import com.example.coreboard.domain.common.exception.FieldError;
import org.springframework.http.HttpStatus;

import java.util.List;

public enum CommentErrorCode {
    COMMENT_NOT_ALLOWED(
            HttpStatus.FORBIDDEN,
            403,
            "댓글 작성이 허용되지 않은 게시글입니다.",
            List.of(new FieldError(
                    "comment, board",
                    "댓글 작성이 허용되지 않은 게시글입니다."
            ))),

    ;

    private final HttpStatus status;
    private final int code;
    private final String message;
    private final List<FieldError> errors;

    CommentErrorCode(
            HttpStatus status,
            int code,
            String message,
            List<FieldError> errors
    ) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.errors = errors;
    }

    public int getCode() {
        return code;
    }

    public List<FieldError> getErrors() {
        return errors;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}