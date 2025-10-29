package com.example.coreboard.domain.auth.controller;

import com.example.coreboard.domain.auth.dto.*;
import com.example.coreboard.domain.auth.service.AuthService;
import com.example.coreboard.domain.common.exception.GlobalExceptionHandler;
import com.example.coreboard.domain.common.exception.auth.AuthErrorCode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
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

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    @DisplayName("회원가입_성공")
    void signUp() throws Exception {
        SignUpDto dummy = new SignUpDto(
                username
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
        String json = """
                    {
                        "username":"user03",
                        "email":"gksthf20@naver.com",
                        "phoneNumber":"02012341234",
                        "password":"gkst",
                        "confirmPassword":"wmleporujq32109"
                    }
                """;
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
        mockMvc.perform(
                        post(BASE + "/users")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이메일은 필수입니다."));
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("회원가입_이메일_null_400")
    void signUpEmailNull() throws Exception {
        String json = """
                {
                    "username":"user03",
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이메일은 필수입니다."));
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("회원가입_Username_누락_400")
    void signUpUsernameRequired() throws Exception {
        String json = """
                    {
                        "username":"",
                        "email":"user03@naver.com",
                        "phoneNumber":"02012341234",
                        "password":"gkst",
                        "confirmPassword":"gkst"
                    }
                """;
        mockMvc.perform(
                        post(BASE + "/users")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("아이디는 필수입니다."));
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("회원가입_Username_null_400")
    void signUpUsernameNull() throws Exception {
        String json = """
                {
                    "email":"user03@naver.com",
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("아이디는 필수입니다."));
        verifyNoInteractions(authService);
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
        mockMvc.perform(
                        post(BASE + "/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호는 필수입니다."));
    }

    @Test
    @DisplayName("회원가입_비밀번호_null_400")
    void signUpPasswordNull() throws Exception {
        String json = """
                {
                    "username" : "user03",
                    "email":"user03@naver.com",
                    "phoneNumber":"02012341234",
                    "confirmPassword":"gkst"
                }
                """;
        mockMvc.perform(
                        post(BASE + "/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호는 필수입니다."));
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("회원가입_비밀번호_확인_누락_400")
    void signUpConfirmPasswordRequired() throws Exception {
        String json = """
                    {
                        "username":"qwer1",
                        "email":"gksthf20@naver.com",
                        "phoneNumber":"02012341234",
                        "password":"qwer1",
                        "confirmPassword":""
                    }
                """;
        mockMvc.perform(
                        post(BASE + "/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호 확인이 일치하지 않습니다."));
    }

    @Test
    @DisplayName("회원가입_비밀번호_확인_null_400")
    void signUpConfirmPasswordNull() throws Exception {
        String json = """
                    {
                        "username":"qwer1",
                        "email":"gksthf20@naver.com",
                        "phoneNumber":"02012341234",
                        "password":"qwer1"
                    }
                """;
        mockMvc.perform(
                        post(BASE + "/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호 확인이 일치하지 않습니다."));
    }

    @Test
    @DisplayName("회원가입_전화번호_누락_400")
    void signUpPhoneNumberRequired() throws Exception{
        String json = """
                    {
                        "username":"qwer1",
                        "email":"gksthf20@naver.com",
                        "phoneNumber":"",
                        "password":"qwer1",
                        "confirmPassword":"qwer1"
                    }
                """;
        mockMvc.perform(
                post(BASE+"/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("전화번호는 필수입니다."));
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("회원가입_전화번호_null_400")
    void signUpPhoneNumberNull() throws Exception{
        String json = """
                    {
                        "username":"qwer1",
                        "email":"gksthf20@naver.com",
                        "password":"qwer1",
                        "confirmPassword":"qwer1"
                    }
                """;
        mockMvc.perform(
                        post(BASE+"/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("전화번호는 필수입니다."));
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("로그인_성공")
    void signIn() throws Exception {
        TokenDto dummy = new TokenDto(
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
    void signIn_IsBadRequest() throws Exception {
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
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호를 필수입니다."));
        verify(authService, never()).signIn(any());
    }

    @Test
    @DisplayName("로그인_필드_passowrd_null_400")
    void signIn_password_IsBadRequest() throws Exception {
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
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호를 필수입니다."));
        verify(authService, never()).signIn(any());
    }

    @Test
    @DisplayName("로그인_password_isBlank")
    void signIn_password_isBlank() throws Exception {
        String json = """
                {
                    "username":"fds"
                }
                """;
        mockMvc.perform(
                        post(BASE + "/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호를 필수입니다."));
        verify(authService, never()).signIn(any());
    }

    @Test
    @DisplayName("로그인_username_isBlank")
    void signIn_username_isBlank() throws Exception {
        String json = """
                {
                    "password":"fds"
                }
                """;
        mockMvc.perform(
                        post(BASE + "/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호를 필수입니다."));
        verify(authService, never()).signIn(any());
    }
}