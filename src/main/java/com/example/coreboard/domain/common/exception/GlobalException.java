package com.example.coreboard.domain.common.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;

public class GlobalException {

    @ExceptionHandler(RuntimeException.class)
    public Object exception(RuntimeException e){
        return e.getMessage();
    }
}
