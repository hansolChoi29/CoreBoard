package com.example.coreboard.domain.common.exception.auth;

import com.example.coreboard.domain.common.exception.FieldError;
import org.springframework.http.HttpStatus;

import java.util.List;

public enum AuthErrorCode {
    BAD_REQUEST(
            HttpStatus.BAD_REQUEST,
            400,
            "비밀번호 또는 아이디가 일치하지 않습니다.",
            List.of(new FieldError(
                    "SIGNIN ID OR PASSWORD MISMATCH",
                    "아이디 또는 비번 불일치입니다."
            ))
    ),
    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            401,
            "다시 로그인해 주세요.",
            List.of(new FieldError(
                    "NoLOGIN",
                    "로그인이 필요합니다."
            ))
    ),
    FORBIDDEN(
            HttpStatus.FORBIDDEN,
            403,
            "접근 권한이 없습니다.",
            List.of(new FieldError(
                    "SIGNIN FORBIDDEN",
                    "접근 권한이 없습니다."
            ))
    ),
    ADMIN_REQUESTER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            404,
            "요청한 사용자를 찾을 수 없습니다.",
            List.of(new FieldError(
                    "ADMIN_REQUESTER_NOT_FOUND",
                    "요청한 사용자를 찾을 수 없습니다."
            ))
    ),
    ADMIN_PERMISSION_REQUIRED(
            HttpStatus.FORBIDDEN,
            403,
            "관리자 권한이 필요한 요청입니다.",
            List.of(new FieldError(
                    "ADMIN_PERMISSION_REQUIRED",
                    "관리자 권한이 필요한 요청입니다."
            ))
    ),
    TARGET_USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            404,
            "권한을 변경할 사용자를 찾을 수 없습니다.",
            List.of(new FieldError(
                    "TARGET_USER_NOT_FOUND",
                    "권한을 변경할 사용자를 찾을 수 없습니다."
            ))
    ),
    LAST_ADMIN_CANNOT_BE_DEMOTED(
            HttpStatus.CONFLICT,
            409,
            "LAST_ADMIN_CANNOT_BE_DEMOTED",
            List.of(new FieldError(
                    "LAST_ADMIN_CANNOT_BE_DEMOTED",
                    "마지막 관리자 계정을 일반 사용자로 변경할 수 없습니다."
            ))
    ),
    NOT_FOUND(
            HttpStatus.NOT_FOUND,
            404,
            "존재하지 않는 사용자입니다.",
            List.of(new FieldError(
                    "USER NOT FOUND",
                    "존재하지 않는 사용자입니다."
            ))
    ),
    PASSWORD_CONFIRM_MISMATCH(
            HttpStatus.BAD_REQUEST,
            400,
            "비밀번호 확인이 일치하지 않습니다.",
            List.of(new FieldError(
                    "SIGNUP PASSWORD MISMATCH",
                    "비밀번호 확인이 일치하지 않습니다."
            ))
    ),
    USERNAME_PASSWORD_ISBLANK(
            HttpStatus.BAD_REQUEST,
            400,
            "아이디 또는 비밀번호를 필수입니다.",
            List.of(new FieldError(
                    "SIGNUP NO ID OR PASSWORD",
                    "아이디 또는 비번 필수입니다."
            ))
    ),
    CONFLICT(
            HttpStatus.CONFLICT,
            409,
            "이미 가입한 계정입니다.",
            List.of(new FieldError(
                    "SIGNUP ID CONFLICT",
                    "이미 가입한 계정입니다."
            ))
    ),
    EMAIL_REQUIRED(
            HttpStatus.BAD_REQUEST,
            400,
            "이메일은 필수입니다.",
            List.of(new FieldError(
                    "SIGNUP NO EMAIL",
                    "이메일은 필수입니다."
            ))
    ),
    ID_REQUIRED(
            HttpStatus.BAD_REQUEST,
            400,
            "아이디는 필수입니다.",
            List.of(new FieldError(
                    "SIGNUP NO ID",
                    "아이디는 필수입니다."
            ))
    ),
    PHONENUMBER_ISBLANK(
            HttpStatus.BAD_REQUEST,
            400,
            "전화번호는 필수입니다.",
            List.of(new FieldError(
                    "SIGNUP NO PHONE NUMBER",
                    "전화번호는 필수입니다."
            ))
    ),
    PASSWORD_REQUIRED(
            HttpStatus.BAD_REQUEST,
            400,
            "비밀번호는 필수입니다.",
            List.of(new FieldError(
                    "SIGNUP NO PASSWORD",
                    "비밀번호는 필수입니다."
            ))
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
