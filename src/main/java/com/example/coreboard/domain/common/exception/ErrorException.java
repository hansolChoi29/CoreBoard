package com.example.coreboard.domain.common.exception;

public abstract class ErrorException extends RuntimeException {
    // 추상 클래스는 필드 선언 + getter + 생성자 다 들어있음 = 설계도 자체

    // 프론트로 내려줄 에러는 항상 status+message 두개를 갖는다. (강제)
    // 전역 핸들러가 이 공통 속성만 믿고 꺼내 쓰면 된다.

    // 왜 RuntimeException 상속할까?
    // try-catch로 만들면 try-catch 지옥

    // 예외에 담을 HTTP 상태 코드
    private final int status;
    // 예외에 담을 메시지
    private final String message;

    // 생성자: 자식 클래스에서 상태와 메시지를 넘겨주면 저장
    public ErrorException(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status; // 상태코드 반환
    }

    public String getMessage() {
        return message;// 메시지 반환
    }
}
