package com.example.coreboard.domain.common.validation;

import com.example.coreboard.domain.auth.dto.SignInRequest;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;

import static com.example.coreboard.domain.common.exception.auth.AuthErrorCode.BAD_REQUEST;

public class AuthValidation {

    private AuthValidation() {
    }

    public static void signInValidation(SignInRequest request) {
        if (
                request.getUsername() == null
                        || request.getUsername().isBlank()
                        || request.getPassword() == null
                        || request.getPassword().isBlank()
        ) {
            throw new AuthErrorException(BAD_REQUEST);
        }
    }
}
