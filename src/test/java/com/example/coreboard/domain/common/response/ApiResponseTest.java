package com.example.coreboard.domain.common.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class ApiResponseTest {
    @Test
    void getMessage() {
        ApiResponse<String> response = ApiResponse.ok("dage", "성공 메시지");
        String msg = response.getMessage();
        assertEquals("성공 메시지", msg);
    }

    @Test
    void getData() {
        ApiResponse<String> response = ApiResponse.ok("dage", "성공 메시지");
        String data = response.getData();
        assertEquals("dage", data);
    }
}