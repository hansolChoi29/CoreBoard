package com.example.coreboard.domain.common.response;

public class ApiResponse<T> {

    private final int status;
    private final String message;
    private final T data;         // 다양한 형태의 데이터를 담을 수 있도록 타입을 제네릭으로

    public ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    //성공
    public static <T> ApiResponse<T> ok(T data, String message) { // ApiResponse 생성자를 호출하여 성공 상태의 응답 객체를 만들어서 반환한다.
        return new ApiResponse<>(200, message, data);
        // 첫 번째 인자 : 상태 문자열 ("SUCCESS")
        // 두 번째 인자 : 실제 응답 데이터
        // 세 번째 인자 : 메시지
    }

    // 실패
    public static ApiResponse<Void> fail(int status, String message) { // ApiResponse 생성자를 호출하여 실패 상태의 응답
        // 객체를 만들어서 반환한다.
        return new ApiResponse<>(status, message, null);
        // 첫 번째 인자 : 상태 문자열 ("FAIL")
        // 두 번째 인자 : 실제 응답 데이터
        // 세 번째 인자 : 메시지
    }

    // 에러
    public static <T> ApiResponse<Void> error(int status, String message) { // ApiResponse 생성자를 호출하여 에러 상태의 응답 객체를
        // 만들어서 반환한다.
        return new ApiResponse<>(status, message, null);
        // 첫 번째 인자 : 상태 문자열 ("ERROR")
        // 두 번째 인자 : 실제 응답 데이터
        // 세 번째 인자 : 메시지
    }
    // 사용 예시 : return ApiResponse.error("예상치 못한 어쩌고 에러발생");

    // JSON 직렬화, 캡슐화, 불변
    public int getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}
