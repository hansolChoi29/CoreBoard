package com.example.coreboard.domain.common.exception;

public abstract class ErrorException extends RuntimeException {

    private final int status;
    private final String message;

    public ErrorException(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

}
