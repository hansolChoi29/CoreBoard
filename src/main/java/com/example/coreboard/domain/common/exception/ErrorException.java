package com.example.coreboard.domain.common.exception;

public abstract class ErrorException extends RuntimeException {

    private final int status;
    private final String message;

    public ErrorException(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    // HTTP Status Code
    // 400 Bad Request -> 해석조차 못하겠다
    // 401 Unauthorized -> 인증 문제, 토큰 없음/만료
    // 403 Forbidden -> 인가 문제, 접근 권한이 없는 다른 유저 시도
    // 404 Not Found
    // 500 Internal Server Error

    // 예시
    // BAD_REQUEST(400,"다시 작성해 주세요."),
    // UNAUTHORIZED(401,"다시 로그인해 주세요."),
    // FORBIDDEN(403, "페이지에 엑세스할 수 없습니다."),
    // NOT_FOUND(404,"페이지를 찾을 수 없습니다."),
    // INTERNAL_SERVER_ERROR(500,"페이지가 작동하지 않습니다.")
}
