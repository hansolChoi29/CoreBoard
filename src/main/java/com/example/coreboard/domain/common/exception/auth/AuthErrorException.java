package com.example.coreboard.domain.common.exception.auth;

import com.example.coreboard.domain.common.exception.ErrorException;

public class AuthErrorException extends ErrorException {

    public AuthErrorException(AuthErrorCode authErrorCode) {
        super(authErrorCode.getStatus(), authErrorCode.getMessage()); // RuntimeException은 message만 받음
    }
}
