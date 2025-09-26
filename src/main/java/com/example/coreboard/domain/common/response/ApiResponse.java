package com.example.coreboard.domain.common.response;

public class ApiResponse<T> {
    private static final String SUCEESS_STAUS="success";
    private static final String FAIL_STATUS="fail";
    private static final String ERROR_STATUS="error";

    private String status;
    private T data;
    private String message;


}
