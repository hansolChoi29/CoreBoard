package com.example.coreboard.domain.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // 이 클래스가 전역 예외 처리 클래스임을 선언
public class GlobalException {

    // GlobalException이 없다면?
    //    {
    //        "timestamp": "2025-09-26T00:00:00.000+00:00",
    //            "status": 500,
    //            "error": "Internal Server Error",
    //            "message": "No message available",
    //            "path": "/auth/login"
    //    }

    @ExceptionHandler(ErrorException.class) // 예외가 터지면 아래 메서드 실행
    public ResponseEntity<ErrorResponseDto> handleErrorException(ErrorException e) { // HTTP응답을 돌려줄 건데, 바디에는
        // ErrorResponseDto객체가 들어간다.
        // ErrorException e: 실제 던져진 예외 객체가 Spring에 의해 주입된다.
        return ResponseEntity //JSON형태로 응답하곘다.
                .status(e.getStatus())
                .body(new ErrorResponseDto(e.getStatus(), e.getMessage())
                );
    }
}
// 응답 구조 다시 짚기
// 1. 응답 라인 <= 상태코드가 들어감
// HTTP/1.1 401 Unauthorized
// 2. 응답 헤더 <= Content-Type, 쿠키, 토큰 같은 메타데이터가 들어감
// Content-Type: application/json
// Content-Leng : 45
// 3. 응답 바디 <= 프론트가 읽는 데이터 (JSON/HTML/XML 등) 들어감
// {"status":401, "message":"다시 로그인해 주세요."}

// GlobalException의 큰 그림
// throw new AuthErrorException(AuthErrorCode.UNAUTHORIZED); 가 서비스, 컨트롤러 어딘가에 존재한다.
// 전여 예외 처리기(GoblaException)가 이 예외를 한 곳에서 잡음
// 예외 안에 이미 들어있는 status+message를 꺼내서 
// 항상 같은 모양의 JSON으로 내려줌
// {"status":401, "message":"다시 로그인해 주세요"}
// 이 구조의 목적 = 일관성, 재사용성, 유지보수성