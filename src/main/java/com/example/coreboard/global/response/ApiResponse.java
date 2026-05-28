package com.example.coreboard.global.response;


public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> fail(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
}
