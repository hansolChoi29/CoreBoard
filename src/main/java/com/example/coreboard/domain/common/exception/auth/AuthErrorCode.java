package com.example.coreboard.domain.common.exception.auth;

public enum AuthErrorCode {
    // enum: 상수를 담아둔 특별한 클래스 (각 상수가 객체처럼 동작) 
    BAD_REQUEST(400, "비밀번호 또는 아이디가 일치하지 않습니다."),
    UNAUTHORIZED(401, "다시 로그인해 주세요."),
    FORBIDDEN(403, "접근 권한이 없습니다."),
    NOT_FOUND(404, "존재하지 않는 사용자입니다."),
    INTERNAL_SERVER_ERROR(500, "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."),
    CONFLICT(409, "이미 가입한 계정입니다.");

    // enum인데 왜 변수랑 생성자가 있을끼?
    // enum의 각 상수를 작은 객체처럼 만들어서 그 안의 상태코드와 메시지를 세트로 보관하려고.

    // getter은 이미 불변인데 왜 굳이 상수로 만들어야 할까?
    // getter은 단순히 불변은 반환하는 기능이다. 값 자체를 누가 언제 어떻게 세팅했는지는 getter가 보장하지 않는다.
    // 즉 객체가 생성될 때 status를 400으로 세팅했는지, 500으로 했는지, 심지어 실행 중에 값이 바뀌어있는지는 getter만으로는 모른다.
    // enum 상수는 불변세트를 보장한다. 즉, status=400, message="잘못된 요청입니다." 고정.

    // 각 코드의 HTTP 상태 값
    private final int status;
    // 사용자에게 보여줄 메시지
    private final String message;

    // enum도 클래스처럼 각 상수에 값을 실어 보낼 수 있다.(생성자 필요)
    AuthErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status; // 상태 코드 반환
    }

    public String getMessage() {
        return message; // 메시지 반환
    }
}
