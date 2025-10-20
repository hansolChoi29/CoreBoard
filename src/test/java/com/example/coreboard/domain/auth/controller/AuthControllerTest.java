package com.example.coreboard.domain.auth.controller;

import com.example.coreboard.domain.auth.service.AuthService;
import com.example.coreboard.domain.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    private static final String BASE ="/auth";
    String username="tester";

    @Mock
    AuthService authService;
    @InjectMocks
    AuthController authController;

    MockMvc mockMvc;

    @BeforeEach
    void setUp(){
        // MockMvc를 만들려면
        // 1. 컨트롤러
        // 2. 전역 예외처리기
        // 3. JSON<->객체 변환기
        // 4. 위 설정들로 MockMvc 인스턴스 생성하겠다.

        mockMvc= MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    @DisplayName("회원가입_성공")
    void signUp() {

    }
    // 회원가입 시 비밀번호 확인 불일치 400
    // 회원가입 시 이미 가입한 계정 409
    // 회원가입 시 이메일 누락 400
    // 회원가입 시 아이디 누락 400
    // 회원가입 시 비밀번호 누락 400
    // 회원가입 시 비밀번호 6자리 이상


    @Test
    void signIn() {
    }
}