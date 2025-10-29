package com.example.coreboard.domain.common.exception;

public abstract class ErrorException extends RuntimeException {

    private final int status;

    public ErrorException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
