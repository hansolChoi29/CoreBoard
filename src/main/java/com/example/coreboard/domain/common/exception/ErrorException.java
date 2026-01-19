package com.example.coreboard.domain.common.exception;


import org.springframework.http.HttpStatus;

import java.util.List;

public abstract class ErrorException extends RuntimeException {
    private final HttpStatus status;
    private final int code;
    private final List<FieldError> errors;

    public ErrorException(
            HttpStatus status,
            int code,
            String message,
            List<FieldError> errors
    ) {
        super(message);
        this.status = status;
        this.code = code;
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
}
