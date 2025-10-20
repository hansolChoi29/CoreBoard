package com.example.coreboard.domain.common.exception.auth;

public enum AuthErrorCode {
    BAD_REQUEST(400, "비밀번호 또는 아이디가 일치하지 않습니다."),
    UNAUTHORIZED(401, "다시 로그인해 주세요."),
    FORBIDDEN(403, "접근 권한이 없습니다."),
    NOT_FOUND(404, "존재하지 않는 사용자입니다."),
    INTERNAL_SERVER_ERROR(500, "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."),
    // 회원가입,
    PASSWORD_CONFIRM_MISMATCH(400, "비밀번호 확인이 일치하지 않습니다."),
    CONFLICT(409, "이미 가입한 계정입니다."),
    EMAIL_REQUIRED(400,"이메일은 필수입니다."),
    ID_REQUIRED(400,"아이디는 필수입니다."),
    PASSWORD_REQUIRED(400,"비밀번호는 필수입니다.");

    private final int status;
    private final String message;

    AuthErrorCode(int status, String message) {
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
