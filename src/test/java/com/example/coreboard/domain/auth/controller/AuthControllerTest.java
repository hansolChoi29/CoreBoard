package com.example.coreboard.domain.auth.controller;

import com.example.coreboard.domain.auth.dto.SignInRequest;
import com.example.coreboard.domain.auth.dto.TokenResponse;
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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    // 시큐리티를 사용하지 않고 직접 구현했으며 filter 사용 대신 인터셉터를 사용했어서 requestAttr()를 씀
    // 만약, 시큐리티(SecurityFilterChain)를 썼다면 @WithMockUser(username = "tester") 하면 됨

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
        given(authService.signUp(any())).willReturn(dummy);
        String json = """
                    {
                        "username":"user03",
                        "email":"gksthf20@naver.com",
                        "phoneNumber":"02012341234",
                        "password":"gkst",
                        "confirmPassword":"gkst"
                    }
                """;
        mockMvc.perform(
                        post(BASE + "/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원가입 성공"));
        verify(authService).signUp(any());
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
                        "confirmPassword":"wmleporujq32109"
                    }
                """; // 컨트롤러 테스트라서 비밀번호 검증은 serviceTest에서 하겠음
        given(authService.signUp(any())).willThrow(new AuthErrorException(AuthErrorCode.PASSWORD_CONFIRM_MISMATCH));

        mockMvc.perform(
                        post(BASE + "/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호 확인이 일치하지 않습니다."));
        verify(authService).signUp(any());
    }

    // 회원가입 시 비밀번호 확인 불일치 400
    @Test
    @DisplayName("회원가입_이미_가입한_계정_409")
    void signUpConflict() throws Exception {
        String json = """
                    {
                        "username":"user03",
                        "email":"gksthf20@naver.com",
                        "phoneNumber":"02012341234",
                        "password":"gkst",
                        "confirmPassword":"gkst"
                    }
                """;
        given(authService.signUp(any())).willThrow(new AuthErrorException(AuthErrorCode.CONFLICT));
        mockMvc.perform(
                        post(BASE + "/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 가입한 계정입니다."));
        verify(authService).signUp(any());
    }

    @Test
    @DisplayName("회원가입_이메일_누락_400")
    void signUpEmailRequired() throws Exception {
        String json = """
                    {
                        "username":"user03",
                        "email":"",
                        "phoneNumber":"02012341234",
                        "password":"gkst",
                        "confirmPassword":"gkst"
                    }
                """;
        given(authService.signUp(any())).willThrow(new AuthErrorException(AuthErrorCode.EMAIL_REQUIRED));
        mockMvc.perform(
                        post(BASE + "/users")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이메일은 필수입니다."));
        verify(authService).signUp(any());
    }

    @Test
    @DisplayName("회원가입_비밀번호_누락_400")
    void signUpPasswordRequired() throws Exception {
        String json = """
                    {
                        "username":"qwer1",
                        "email":"gksthf20@naver.com",
                        "phoneNumber":"02012341234",
                        "password":"",
                        "confirmPassword":""
                    }
                """;
        given(authService.signUp(any())).willThrow(new AuthErrorException(AuthErrorCode.PASSWORD_REQUIRED));
        mockMvc.perform(
                        post(BASE + "/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호는 필수입니다."));
        verify(authService).signUp(any());
    }

    @Test
    @DisplayName("로그인_성공")
    void signIn() throws Exception {
        TokenResponse dummy = new TokenResponse(
                "accessToken",
                "refreshToken"
        );
        given(authService.signIn(any())).willReturn(dummy);
        String json = """
                {
                    "username" : "qwer1",
                    "password" : "qwer1"
                }
                """;
        mockMvc.perform(
                        post(BASE + "/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그인 성공!"))
                .andExpect(jsonPath("$.data.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.data.refreshToken").value("refreshToken"));
        verify(authService).signIn(any());
    }

    //로그인 시나리오 (컨트롤러는 요청 응답 메시지용, 진짜 검증은 서비스test에서)
    @Test
    @DisplayName("로그인_존재하지_않는_사용자_404")
    void signInNotFound() throws Exception {

        given(authService.signIn(any())).willThrow(new AuthErrorException(AuthErrorCode.NOT_FOUND));
        String json = """
                 {
                    "username" : "qwer1",
                    "password" : "qwer1"
                }
                """;
        mockMvc.perform(
                        post(BASE + "/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));
        verify(authService).signIn(any());
    }

    @Test
    @DisplayName("로그인_아이디_또는_비밀번호_불일치")
    void signInIdOrPasswordMismatch() throws Exception {
        String json = """
                {
                    "username" : "username",
                    "password" : "password"
                }
                """;
        given(authService.signIn(any())).willThrow(new AuthErrorException(AuthErrorCode.BAD_REQUEST));
        mockMvc.perform(
                        post(BASE + "/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호 또는 아이디가 일치하지 않습니다."));
        verify(authService).signIn(any());
    }

    @Test
    @DisplayName("로그인_필드_null_400")
    void signIn_IsBadRequest() throws Exception{
        String json = """
                {
                 "username" : "",
                 "password" : ""
                }
                """;
        mockMvc.perform(
                        post(BASE + "/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호 또는 아이디가 일치하지 않습니다."));
        verify(authService, never()).signIn(any());
    }
    @Test
    @DisplayName("로그인_필드_passowrd_null_400")
    void signIn_password_IsBadRequest() throws Exception{
        String json = """
                {
                 "username" : "fdsa",
                 "password" : ""
                }
                """;
        mockMvc.perform(
                        post(BASE + "/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호 또는 아이디가 일치하지 않습니다."));
        verify(authService, never()).signIn(any());
    }

    @Test
    @DisplayName("로그인_password_isBlank")
    void signIn_password_isBlank() throws Exception{
        String json = """
                {
                    "username":"fds"
                }
                """;
        mockMvc.perform(
                post(BASE+"/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호 또는 아이디가 일치하지 않습니다."));
        verify(authService, never()).signIn(any());
    }
    @Test
    @DisplayName("로그인_username_isBlank")
    void signIn_username_isBlank() throws Exception{
        String json = """
                {
                    "password":"fds"
                }
                """;
        mockMvc.perform(
                        post(BASE+"/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호 또는 아이디가 일치하지 않습니다."));
        verify(authService, never()).signIn(any());
    }
}