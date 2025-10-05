package com.example.coreboard.domain.common.exception;

import com.example.coreboard.domain.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 예외 처리기(전역에서 던진 예외를 가로채 응답으로 변환)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleFailException(ErrorException e) { // <String>은 data 타입을 문자열로
        // 보내겠다.
        return ResponseEntity
                .status(e.getStatus())
                .body(ApiResponse.fail(e.getStatus(), e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleErrorException(ErrorException e) {

        return ResponseEntity
                .status(e.getStatus())
                .body(ApiResponse.error(e.getStatus(), e.getMessage()));
    }
}
