package com.example.coreboard.domain.auth.exception;

import com.example.coreboard.global.exception.ErrorException;

public class AuthErrorException extends ErrorException {

    public AuthErrorException(AuthErrorCode authErrorCode) {
        super(
                authErrorCode.getStatus(),
                authErrorCode.getCode(),
                authErrorCode.getMessage(),
                authErrorCode.getErrors()
                );
    }
}
