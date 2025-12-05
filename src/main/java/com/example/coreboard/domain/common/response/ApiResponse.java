package com.example.coreboard.domain.common.response;

import java.util.Collections;

public class ApiResponse<T> {
    private final String message;
    private final T data;

    public ApiResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(message, data);
    }

    public static ApiResponse<Object> fail(String message) {
        return new ApiResponse<>(message, Collections.emptyMap());
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}
