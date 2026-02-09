package com.example.coreboard.domain.common.exception;

public class FieldError {
    private final String field;
    private final String reason;

    public FieldError(String field, String reason) {
        this.field = field;
        this.reason = reason;
    }

    public String getField() {
        return field;
    }

    public String getReason() {
        return reason;
    }
}
