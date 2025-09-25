package com.example.coreboard.domain.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(ErrorException.class)
    public ResponseEntity<ErrorResponseDto> handleErrorException(ErrorException e) {
        return ResponseEntity.status(
            e.getStatus()
        ).body(
            new ErrorResponseDto(
                e.getStatus(), e.getMessage()
            )
        );
    }
}
