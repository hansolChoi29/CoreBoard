package com.example.coreboard.domain.common.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class ApiResponseTest {
    @Test
    void getMessage() {
        ApiResponse<String> response = ApiResponse.ok("dage", "성공 메시지");

        String msg = response.getMessage();
        System.out.println(">>> getMessage() 결과 = " + msg);

        assertEquals("성공 메시지", msg);
    }

    @Test
    void getData() {
        ApiResponse<String> response = ApiResponse.ok("dage", "성공 메시지");

        String data = response.getData();
        System.out.println(">>> getData() 결과 = " + data);

        assertEquals("dage", data);
    }
    // 왜 안돼?..
}