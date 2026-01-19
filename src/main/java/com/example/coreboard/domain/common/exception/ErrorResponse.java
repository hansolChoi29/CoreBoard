package com.example.coreboard.domain.common.exception;

import java.util.List;

public class ErrorResponse {
    private final String code;
    private final List<FieldError> errors;

    public ErrorResponse(String code, List<FieldError> errors) {
        this.code = code;
        this.errors = errors;
    }

    public List<FieldError> getErrors() {
        return errors;
    }

    public String getCode() {
        return code;
    }
}
