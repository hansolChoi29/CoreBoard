package com.example.coreboard.domain.common.exception.auth;

import com.example.coreboard.domain.common.exception.FieldError;
import org.springframework.http.HttpStatus;

import java.util.List;

public enum AuthErrorCode {
    BAD_REQUEST(
            HttpStatus.BAD_REQUEST,
            400,
            "비밀번호 또는 아이디가 일치하지 않습니다.",
            List.of()
    ),
    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            401,
            "다시 로그인해 주세요.",
            List.of()
    ),
    FORBIDDEN(
            HttpStatus.FORBIDDEN,
            403,
            "접근 권한이 없습니다.",
            List.of()
    ),
    NOT_FOUND(
            HttpStatus.NOT_FOUND,
            404,
            "존재하지 않는 사용자입니다.",
            List.of()
    ),
    PASSWORD_CONFIRM_MISMATCH(
            HttpStatus.BAD_REQUEST,
            400,
            "비밀번호 확인이 일치하지 않습니다.",
            List.of()
    ),
    USERNAME_PASSWORD_ISBLANK(
            HttpStatus.BAD_REQUEST,
            400,
            "아이디 또는 비밀번호를 필수입니다.",
            List.of()
    ),
    CONFLICT(
            HttpStatus.CONFLICT,
            409,
            "이미 가입한 계정입니다.",
            List.of()
    ),
    EMAIL_REQUIRED(
            HttpStatus.BAD_REQUEST,
            400,
            "이메일은 필수입니다.",
            List.of()
    ),
    ID_REQUIRED(
            HttpStatus.BAD_REQUEST,
            400,
            "아이디는 필수입니다.",
            List.of()
    ),
    PHONENUMBER_ISBLANK(
            HttpStatus.BAD_REQUEST,
            400,
            "전화번호는 필수입니다.",
            List.of()
    ),
    PASSWORD_REQUIRED(
            HttpStatus.BAD_REQUEST,
            400,
            "비밀번호는 필수입니다.",
            List.of()
    );

    private final HttpStatus status;
    private final int code;
    private final List<FieldError> errors;
    private final String message;

    AuthErrorCode(
            HttpStatus status,
            int code,
            String message,
            List<FieldError> errors
    ) {
        this.status = status;
        this.code = code;
        this.message = message;
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

    public String getMessage() {
        return message;
    }
}
