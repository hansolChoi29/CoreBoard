package com.example.coreboard.domain.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleMemberException(ErrorException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(new ErrorResponseDto(e.getStatus(), e.getMessage())
                );
    }
}
