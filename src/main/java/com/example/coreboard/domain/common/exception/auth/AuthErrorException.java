package com.example.coreboard.domain.common.exception.auth;

import com.example.coreboard.domain.common.exception.ErrorException;

public class AuthErrorException extends ErrorException {
    private final AuthErrorCode authErrorCode;

    public AuthErrorException(AuthErrorCode authErrorCode) {
        super(authErrorCode.getStatus(), authErrorCode.getMessage());
        this.authErrorCode=authErrorCode;
    }
}
