package com.example.coreboard.domain.common.response;

public class ApiResponse<T> {
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String FAIL_STATUS = "FAIL";
    private static final String ERROR_STATUS = "ERROR";

    private String status;
    private T data;         // 다양한 형태의 데이터를 담을 수 있도록 타입을 제네릭으로
    private String message;

    public ApiResponse(String status, T data, String message) {
        this.status = status;
        this.data = data = data;
        this.message = message;
    }

    //성공
    public static <T> ApiResponse<T> ok(T data, String message) { // ApiResponse 생성자를 호출하여 성공 상태의 응답 객체를 만들어서 반환한다.
        return new ApiResponse<>(SUCCESS_STATUS, data, message);
        // 첫 번째 인자 : 상태 문자열 ("SUCCESS")
        // 두 번째 인자 : 실제 응답 데이터
        // 세 번째 인자 : 메시지
    }

    // 실패
    public static <T> ApiResponse<T> fail(T data, String message) { // ApiResponse 생성자를 호출하여 실패 상태의 응답 객체를 만들어서 반환한다.
        return new ApiResponse<>(FAIL_STATUS, data, message);
        // 첫 번째 인자 : 상태 문자열 ("FAIL")
        // 두 번째 인자 : 실제 응답 데이터
        // 세 번째 인자 : 메시지
    }

    // 에러
    public static <T> ApiResponse<T> error(T data, String message) { // ApiResponse 생성자를 호출하여 에러 상태의 응답 객체를 만들어서 반환한다.
        return new ApiResponse<>(ERROR_STATUS, data, message);
        // 첫 번째 인자 : 상태 문자열 ("ERROR")
        // 두 번째 인자 : 실제 응답 데이터
        // 세 번째 인자 : 메시지
    }
    // 사용 예시 : return ApiResponse.error("예상치 못한 어쩌고 에러발생");

    // JSON 직렬화, 캡슐화, 불변
    public String getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}
