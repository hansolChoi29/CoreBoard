package com.example.coreboard.domain.common.exception;

import com.example.coreboard.domain.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 예외 처리기(전역에서 던진 예외를 가로채 응답으로 변환)
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 성공 응답은 컨트롤에서 직접 반환하기 때문에 예외 처리기에서 다루지 않음

    // ResponseEntity: HTTP 응답 껍데기(상태코드/헤더/바디)
    // ApiResponse<Object> : 바디에 넣을 공통 포맷 {"message" : "","data" : {}}
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleFailException(ErrorException e) {
        // ResponseEntity는 HTTP 응답 객체고,
        // 안에 ApiResponse을 담겠다, Void는 status+message만 보내겠다.
        return ResponseEntity
                .status(e.getStatus())                   // 응답 상태코드 400, 404, 405 등
                .body(ApiResponse.fail(e.getMessage())); // 에러 메시지와 응답 코드를 body에 담겠다.
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleErrorException(ErrorException e) {
        // ResponseEntity는 HTTP 응답 객체고,
        // 안에 ApiResponse을 담겠다, Void는 status+message만 보내겠다.
        return ResponseEntity
                .status(e.getStatus())                        //500 서버 에러
                .body(ApiResponse.error(e.getMessage()));    // 에러 메시지와 응답 코드를 body에 담겠다.
    }
}
