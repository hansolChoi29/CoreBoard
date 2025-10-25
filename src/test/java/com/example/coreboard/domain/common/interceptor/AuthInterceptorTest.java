package com.example.coreboard.domain.common.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import static org.junit.jupiter.api.Assertions.*;

class AuthInterceptorTest {
    AuthInterceptor authInterceptor;
    MockHttpServletRequest mockHttpServletRequest;
    MockHttpServletResponse mockHttpServletResponse;
    HandlerMethod handlerMethod;

    // 컨트롤러 실행 전에 preHandle()이 제대로 동작하는지 확인
    // 즉, HTTP 요청을 가짜(MockHttpServletRequest)로 만들어서 
    // preHandle() 메서드 직접 호출해보는 단위 테스트

    // 요청 전 JWT 검증
    // 테스트 도구는 MockHttpServletRequest, MockHttpServletResponse
    // 테스트 시점 : 컨트롤러 진입 전
    // 토큰 검증과 인증 통과 여부 확인
    // interceptor.preHandle()로 실행

    // 목료 3가지
    // Authorization 헤더가 없으면 예외 발생하는지
    // 유효하지 않은 토큰이면 예외 발생하는지
    // 유효한 토큰이면 username이 request에 저장되는지

    @BeforeEach
        // 병렬 실행일 수도 있고 인텔리제이는 순서를 보장하지 않기 때문에 지정해야 함
        // 초기화 : 모든 테스트 메서드가 실행되기 전 반드시 한 번씩 실행!
    void setUp() throws NoSuchMethodException {
        // 테스트할 대상 직접 생성 - AuthInterceptor 인스턴스
        authInterceptor = new AuthInterceptor();
        // 가짜 요청 객제 - 헤더, url, 메서드 등?
        // MockHttpServletRequest : 실제 HTTP 요청처럼 헤더, 메서드, url 세팅할 수 있는 객체
        mockHttpServletRequest = new MockHttpServletRequest();
        // 가짜 응답 객체 - 상태코드
        // MockHttpServletResponse : 실제 HTTP 응답처럼 상태코드, 바디를 확인할 수 있는 객체
        mockHttpServletResponse = new MockHttpServletResponse();

        // 이 테스트 파일 자기 자신 안에 있는 setUp()이라는 메서드를 가짜로 꺼내서
        // 스프링이 넘겨주는 컨트롤러 정보인 척하기 위함
        // HandlerMethod
        handlerMethod = new HandlerMethod(
                this, // 이 테스트 클래스 자기 자신
                this.getClass().getDeclaredMethod("setUp")
        );
        // 위 코드가 필요한 이유: 인터셉터는 원래 스프링이 실행해주는 거라서
        // 스프링이 자동으로 handler라는 정보를 넘겨줌
        // 테스트는 스프링 없이 실행해야 하니까 흉내내는 코드가 handlerMethod임
    }

    @Test
    @DisplayName("Authorization_헤더_없음_GET통과")
    void noHeader_getRequestIsPass() {

        // GET이면 true
        mockHttpServletRequest.setMethod("GET");
        // assertDoesNotThrow : 예외 없이 정상 실행되어야 한다는 걸 테스트
        assertDoesNotThrow(() -> authInterceptor.preHandle(
                        mockHttpServletRequest,
                        mockHttpServletResponse,
                        handlerMethod
                )
        );
    }

    @Test
    @DisplayName("Authorization_헤더_없음_POST예외")
    void noHeader_postRequest_fail() {

    }

    @Test
    @DisplayName("유효하지_않은_토큰_예외발생")
    void invalidToken_fali() {

    }

    @Test
    @DisplayName("유효한_토큰_username저장")
    void validToken_success() throws Exception {
        // 참고로 throws Exceptio 대신 try-catch 써도 됨
        // 차이점
        // throws Exceptio : 예외가 터질 수도 있음 (가능성)
        // try-catch : 예외를 직접 잡아서 처리
    }
}