package com.example.coreboard.domain.common.exception.auth;

import com.example.coreboard.domain.common.exception.ErrorException;

// 인증 관련 예외를 나타내는 구체 클래스
public class AuthErrorException extends ErrorException {

    // 부모의 status, message 필드를 그대로 몰려 받는다
    // 그래서 선언할 필요가 없음

    // 생성자: AuthErrorCode(enum) 객체를 받아서 
    public AuthErrorException(AuthErrorCode authErrorCode) {
        // 부모 ErrorException에 상태 코드와 메시지를 전달
        super(authErrorCode.getStatus(), authErrorCode.getMessage());
    }
}

// 자바 객체는 크게 두 가지 종류의 예외를 제공한다.
// 1) Checked Exception (체크 예외)
// 반드시 try-catch 해야 하는 예외
// 컴파일러가 강제로 검사해야 함
// 2) Unchecked Exception
// try-catch 안 해도 됨
// 실행 중 발생하는 에러

// 그렇다면 ErrorException는 뭔데?
// 커스텀 예외 클래스.