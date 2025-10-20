package com.example.coreboard.domain.auth.controller;

import com.example.coreboard.domain.auth.service.AuthService;
import com.example.coreboard.domain.common.exception.GlobalExceptionHandler;
import com.example.coreboard.domain.common.exception.auth.AuthErrorCode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.users.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.print.attribute.standard.Media;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    private static final String BASE = "/auth";
    String username = "tester";

    @Mock
    AuthService authService;
    @InjectMocks
    AuthController authController;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // MockMvc를 만들려면
        // 1. 컨트롤러
        // 2. 전역 예외처리기
        // 3. JSON<->객체 변환기
        // 4. 위 설정들로 MockMvc 인스턴스 생성하겠다.

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    @DisplayName("회원가입_성공")
    void signUp() throws Exception {
        Users dummy = new Users(
                username,
                "qwerqweqr1",
                "qwer29@naver.com",
                "01012341234"
        );
        String json = """
                    {
                        "username":"user03",
                        "email":"gksthf20@naver.com",
                        "phoneNumber":"02012341234",
                        "password":"gkst",
                        "confirmPassword":"gkst"
                    }
                """;
        given(authService.signup(any())).willReturn(dummy);
        mockMvc.perform(
                        post(BASE + "/users")
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원가입 성공"));
        verify(authService).signup(any());
    }

    @Test
    @DisplayName("회원가입_비밀번호_확인_불일치_400")
    void signUpConfirmMismatch() throws Exception {
        // 요청 바디에 넣을 json
        String json = """
                    {
                        "username":"user03",
                        "email":"gksthf20@naver.com",
                        "phoneNumber":"02012341234",
                        "password":"gkst",
                        "confirmPassword":"gkst"
                    }
                """; // 컨트롤러 테스트라서 비밀번호 검증은 serviceTest에서 하겠음
        given(authService.signup(any())).willThrow(new AuthErrorException(AuthErrorCode.PASSWORD_CONFIRM_MISMATCH));
        mockMvc.perform(
                        post(BASE + "/users")
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호 확인이 일치하지 않습니다."));
        verify(authService).signup(any());
    }
    // 회원가입 시 비밀번호 확인 불일치 400
    @Test
    @DisplayName("회원가입_이미_가입한_계정_409")
    void signUpConflict() throws Exception{
        String json = """
                    {
                        "username":"user03",
                        "email":"gksthf20@naver.com",
                        "phoneNumber":"02012341234",
                        "password":"gkst",
                        "confirmPassword":"gkst"
                    }
                """;
        given(authService.signup(any())).willThrow(new AuthErrorException(AuthErrorCode.CONFLICT));
        mockMvc.perform(
                post(BASE+"/users")
                        .requestAttr("username", username)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 가입한 계정입니다."));
        verify(authService).signup(any());
    }
    // 회원가입 시 이미 가입한 계정 409

    // 회원가입 시 이메일 누락 400
    // 회원가입 시 아이디 누락 400
    // 회원가입 시 비밀번호 누락 400
    // 회원가입 시 비밀번호 6자리 이상


    @Test
    void signIn() {
    }
}