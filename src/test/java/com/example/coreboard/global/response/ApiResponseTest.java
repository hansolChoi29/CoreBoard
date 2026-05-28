package com.example.coreboard.global.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class ApiResponseTest {
    @Test
    void getMessage() {
        ApiResponse<String> response = ApiResponse.ok("dage", "성공 메시지");
        String msg = response.message();
        assertEquals("성공 메시지", msg);
    }

    @Test
    void getData() {
        ApiResponse<String> response = ApiResponse.ok("dage", "성공 메시지");
        String data = response.data();
        assertEquals("dage", data);
    }
}