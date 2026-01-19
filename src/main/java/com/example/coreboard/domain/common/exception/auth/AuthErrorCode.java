package com.example.coreboard.domain.common.exception.auth;

import com.example.coreboard.domain.common.exception.FieldError;
import org.springframework.http.HttpStatus;

import java.util.List;

public enum AuthErrorCode {
    BAD_REQUEST(400, "비밀번호 또는 아이디가 일치하지 않습니다."),
    UNAUTHORIZED(401, "다시 로그인해 주세요."),
    FORBIDDEN(403, "접근 권한이 없습니다."),
    NOT_FOUND(404, "존재하지 않는 사용자입니다."),
    PASSWORD_CONFIRM_MISMATCH(400, "비밀번호 확인이 일치하지 않습니다."),
    USERNAME_PASSWORD_ISBLANK(400, "아이디 또는 비밀번호를 필수입니다."),
    CONFLICT(409, "이미 가입한 계정입니다."),
    EMAIL_REQUIRED(400, "이메일은 필수입니다."),
    ID_REQUIRED(400, "아이디는 필수입니다."),
    PHONENUMBER_ISBLANK(400, "PHONENUMBER_ISBLANK", "전화번호는 필수입니다."),
    PASSWORD_REQUIRED(400, "PASSWORD_REQUIRED", "비밀번호는 필수입니다.", );

    private final HttpStatus status;
    private final int code;
    private final List<FieldError> errors;
    private final String message;

    AuthErrorCode(
            HttpStatus status,
            int code,
            List<FieldError> errors,
            String message
    ) {
        this.status = status;
        this.code = code;
        this.errors = errors;
        this.message = message;
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
