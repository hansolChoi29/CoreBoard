package com.example.coreboard.domain.common.exception.board;

import com.example.coreboard.domain.common.exception.FieldError;
import org.springframework.http.HttpStatus;

import java.util.List;

public enum BoardErrorCode {
    BOARD_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            404,
            "제목과 내용은 필수입니다.",
            List.of(new FieldError(
                    "NO TITLE AND CONTENT",
                    "제목과 본문은 필수입니다."
            ))

    );

    private final HttpStatus status;
    private final int code;
    private final String message;
    private final List<FieldError> errors;

    BoardErrorCode(
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
