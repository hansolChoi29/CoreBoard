package com.example.coreboard.domain.common.exception;

import com.example.coreboard.domain.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 예외 처리기(전역에서 던진 예외를 가로채 응답으로 변환)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<T>> handleMemberException(ErrorException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(ApiResponse.fail(data,e.getMessage()));
    }
}
