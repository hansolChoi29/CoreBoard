package com.example.coreboard.domain.common.validation;

import com.example.coreboard.domain.auth.dto.request.SignInRequest;
import com.example.coreboard.domain.auth.dto.request.SignUpRequest;
import com.example.coreboard.domain.common.exception.auth.AuthErrorCode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;

import static com.example.coreboard.domain.common.exception.auth.AuthErrorCode.USERNAME_PASSWORD_ISBLANK;

public class AuthValidation {

    public static void signUpValidation(SignUpRequest request) {
        signUp(request.username(),
                request.password(),
                request.confirmPassword(),
                request.email(),
                request.phoneNumber());
    }

    public static void signInValidation(SignInRequest resquest) {
        signIn(resquest.username(), resquest.password());
    }

    public static void signUp(
            String username,
            String password,
            String confirmPassword,
            String email,
            String phoneNumber) {
        if (username == null || username.isBlank()) {
            throw new AuthErrorException(AuthErrorCode.ID_REQUIRED);
        }

        if (password == null || password.isBlank()) {
            throw new AuthErrorException(AuthErrorCode.PASSWORD_REQUIRED);
        }

        if (confirmPassword == null || confirmPassword.isBlank()) {
            throw new AuthErrorException(AuthErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }

        if (email == null || email.isBlank()) {
            throw new AuthErrorException(AuthErrorCode.EMAIL_REQUIRED);
        }

        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new AuthErrorException(AuthErrorCode.PHONENUMBER_ISBLANK);
        }
    }

    public static void signIn(String username, String password) {
        if (username == null
                || username.isBlank()
                || password == null
                || password.isBlank()) {
            throw new AuthErrorException(USERNAME_PASSWORD_ISBLANK);
        }
    }
}
