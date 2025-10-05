package com.example.coreboard.domain.common.exception;

// 공통 예외 베이스 (양식)
public abstract class ErrorException extends RuntimeException {

    private final int status; // HTTP 상태 코드
    // private final String message; RuntimeException를 상속 받을 의미가 없음

    public ErrorException(int status, String message) {
        super(message);         // 표준 메시지에 저장
        this.status = status;
    }

    public int getStatus() { // 예외는 컨트롤러 밖(전역 핸들러)에서 처리된까 핸들러가 HTTP 상태 코드를 읽어야 한다
        return status;      // 전역 핸들러에서 쓰려고 게터
    }
}
