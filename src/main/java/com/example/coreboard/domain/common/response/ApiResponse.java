package com.example.coreboard.domain.common.response;

import java.util.Collections;

public class ApiResponse<T> {

    private final String message;
    private final T data;

    public ApiResponse( String message, T data) {
        this.message = message;
        this.data = data;
    }

    // 성공
    public static <T> ApiResponse<T> ok(T data, String message) {   // ApiResponse 생성자를 호출하여 성공 상태의 응답 객체를 만들어서 반환한다.
        return new ApiResponse<>( message, data);
        // 두 번째 인자 : 메시지
        // 세 번째 인자 : 데이터 페이로드
    }
    
    // 400, 500 나눈 이유
    // 클라이언트와 서버 - 책임을 나눔

    // 실패 - 사용자 실수(400)
    public static ApiResponse<Object> fail( String message) {   // ApiResponse 생성자를 호출하여 실패 상태의 응답
                                                                // 객체를 만들어서 반환한다, Void로 하면 데이터가 null로 나오므로 Object로 수정함
        return new ApiResponse<>( message, Collections.emptyMap());
        // 두 번째 인자 : 메시지
        // 세 번째 인자 : 데이터 페이로드 data:{}
    }

    // 서버 에러 (500)
    public static <T> ApiResponse<Object> error(String message) {   // ApiResponse 생성자를 호출하여 에러 상태의 응답 객체를
                                                                    // 만들어서 반환한다, Void로 하면 데이터가 null로 나오므로 Object로 수정함
        return new ApiResponse<>( message, Collections.emptyMap());
        // 두 번째 인자 : 메시지
        // 세 번째 인자 : 데이터 페이로드 data:{}
    }
    // 사용 예시 : return ApiResponse.error("예상치 못한 어쩌고 에러발생");

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}
