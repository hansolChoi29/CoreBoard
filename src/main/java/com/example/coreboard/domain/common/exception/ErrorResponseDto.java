package com.example.coreboard.domain.common.exception;

public class ErrorResponseDto {

    // ErrorException이 있는데 왜 ErrorResponseDto가 필요할까?
    // dto는 클라이언트에게
    // ErrorException은 스프링 서버 안에서만 쓰는 용도

    // HTTP 상태코드
    private int status;
    // 클라이언트에게 보여줄 오류 메시지
    private String message;

    // 생성자: 객체 만들 때 상태코드와 메시지 함께 보여주겠다.
    public ErrorResponseDto(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status; // 값을 꺼냄
    }

    public String getMessage() {
        return message; // 값을 꺼냄
    }
}