package com.example.coreboard.domain.auth.controller;

import com.example.coreboard.domain.auth.dto.*;
import com.example.coreboard.domain.auth.dto.request.SignInRequest;
import com.example.coreboard.domain.auth.dto.request.SignUpRequest;
import com.example.coreboard.domain.auth.service.AuthService;
import com.example.coreboard.domain.common.exception.GlobalExceptionHandler;
import com.example.coreboard.domain.common.exception.auth.AuthErrorCode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeAll;
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

import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {
    ObjectMapper objectMapper = new ObjectMapper();

    private static final String BASE = "/auth";
    String username = "tester";

    @Mock
    AuthService authService;
    @InjectMocks
    AuthController authController;

    MockMvc mockMvc;

    @BeforeAll
    static void setUpJwt() {
        JwtUtil.init("this-is-a-very-very-long-test-secret-key-over-32byte");
    }

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
                username);
        given(authService.signUp(any())).willReturn(dummy);
        SignUpRequest request = new SignUpRequest("user03", "gkst", "gkst", "gksthf20@naver.com",
                "02012341234");
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(
                        post(BASE + "/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원가입 성공"));
        verify(authService).signUp(any());
    }

    @Test
    @DisplayName("회원가입_비밀번호_확인_불일치_400")
    void signUpConfirmMismatch() throws Exception {
        SignUpRequest request = new SignUpRequest("user03", "gkst", "gkwst", "gksthf20@naver.com",
                "02012341234");
        String json = objectMapper.writeValueAsString(request);
        given(authService.signUp(any()))
                .willThrow(new AuthErrorException(AuthErrorCode.PASSWORD_CONFIRM_MISMATCH));

        mockMvc.perform(
                        post(BASE + "/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호 확인이 일치하지 않습니다."));
        verify(authService).signUp(any());
    }

    @Test
    @DisplayName("회원가입_이미_가입한_계정_409")
    void signUpConflict() throws Exception {
        SignUpRequest request = new SignUpRequest("user03", "gkst", "gkst", "gksthf20@naver.com",
                "02012341234");
        String json = objectMapper.writeValueAsString(request);

        given(authService.signUp(any())).willThrow(new AuthErrorException(AuthErrorCode.CONFLICT));
        mockMvc.perform(
                        post(BASE + "/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 가입한 계정입니다."));
        verify(authService).signUp(any());
    }

    @Test
    @DisplayName("회원가입_이메일_누락_400")
    void signUpEmailRequired() throws Exception {
        SignUpRequest request = new SignUpRequest("user03", "gkst", "gkst", "",
                "02012341234");
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        post(BASE + "/users")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이메일은 필수입니다."));
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("회원가입_이메일_null_400")
    void signUpEmailNull() throws Exception {
        SignUpRequest request = new SignUpRequest("user03", "gkst", "gkst", null,
                "02012341234");
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        post(BASE + "/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이메일은 필수입니다."));
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("회원가입_Username_누락_400")
    void signUpUsernameRequired() throws Exception {

        SignUpRequest request = new SignUpRequest("", "gkst", "gkst", "user03@naver.com",
                "02012341234");
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        post(BASE + "/users")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("아이디는 필수입니다."));
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("회원가입_Username_null_400")
    void signUpUsernameNull() throws Exception {
        SignUpRequest request = new SignUpRequest(null, "gkst", "gkst", "user03@naver.com",
                "02012341234");
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        post(BASE + "/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("아이디는 필수입니다."));
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("회원가입_비밀번호_누락_400")
    void signUpPasswordRequired() throws Exception {
        SignUpRequest request = new SignUpRequest("qwer1", "", "", "user03@naver.com",
                "02012341234");
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(
                        post(BASE + "/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호는 필수입니다."));
    }

    @Test
    @DisplayName("회원가입_비밀번호_null_400")
    void signUpPasswordNull() throws Exception {
        SignUpRequest request = new SignUpRequest("qwer1", null, null, "user03@naver.com",
                "02012341234");
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(
                        post(BASE + "/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호는 필수입니다."));
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("회원가입_비밀번호_확인_누락_400")
    void signUpConfirmPasswordRequired() throws Exception {
        SignUpRequest request = new SignUpRequest("qwer1", "qwer1", "", "user03@naver.com",
                "02012341234");
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        post(BASE + "/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호 확인이 일치하지 않습니다."));
    }

    @Test
    @DisplayName("회원가입_비밀번호_확인_null_400")
    void signUpConfirmPasswordNull() throws Exception {
        SignUpRequest request = new SignUpRequest("qwer1", "qwer1", null, "user03@naver.com",
                "02012341234");
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        post(BASE + "/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호 확인이 일치하지 않습니다."));
    }

    @Test
    @DisplayName("회원가입_전화번호_누락_400")
    void signUpPhoneNumberRequired() throws Exception {
        SignUpRequest request = new SignUpRequest("qwer1", "qwer1", "qwer1", "user03@naver.com",
                "");
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        post(BASE + "/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("전화번호는 필수입니다."));
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("회원가입_전화번호_null_400")
    void signUpPhoneNumberNull() throws Exception {
        SignUpRequest request = new SignUpRequest("qwer1", "qwer1", "qwer1", "user03@naver.com",
                null);
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        post(BASE + "/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("전화번호는 필수입니다."));
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("로그인_성공")
    void signIn() throws Exception {
        TokenDto dummy = new TokenDto(
                "accessToken",
                "refreshToken");
        given(authService.signIn(any())).willReturn(dummy);
        SignInRequest request = new SignInRequest("user01", "password");
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        post(BASE + "/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그인 성공!"))
                .andExpect(jsonPath("$.data.accessToken").value("accessToken"));
        verify(authService).signIn(any());
    }

    @Test
    @DisplayName("로그인_존재하지_않는_사용자_404")
    void signInNotFound() throws Exception {
        given(authService.signIn(any())).willThrow(new AuthErrorException(AuthErrorCode.NOT_FOUND));
        SignInRequest request = new SignInRequest("user01", "password");
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        post(BASE + "/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));
        verify(authService).signIn(any());
    }

    @Test
    @DisplayName("로그인_아이디_또는_비밀번호_불일치")
    void signInIdOrPasswordMismatch() throws Exception {
        SignInRequest request = new SignInRequest("user01", "password");
        String json = objectMapper.writeValueAsString(request);
        given(authService.signIn(any())).willThrow(new AuthErrorException(AuthErrorCode.BAD_REQUEST));
        mockMvc.perform(
                        post(BASE + "/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호 또는 아이디가 일치하지 않습니다."));
        verify(authService).signIn(any());
    }

    @Test
    @DisplayName("로그인_필드_null_400")
    void signIn_IsBadRequest() throws Exception {
        SignInRequest request = new SignInRequest(null, null);
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        post(BASE + "/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호를 필수입니다."));
        verify(authService, never()).signIn(any());
    }

    @Test
    @DisplayName("로그인_필드_passowrd_null_400")
    void signIn_password_IsBadRequest() throws Exception {
        SignInRequest request = new SignInRequest("user01", null);
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        post(BASE + "/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호를 필수입니다."));
        verify(authService, never()).signIn(any());
    }

    @Test
    @DisplayName("로그인_password_isBlank")
    void signIn_password_isBlank() throws Exception {
        SignInRequest request = new SignInRequest("user01", "");
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        post(BASE + "/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호를 필수입니다."));
        verify(authService, never()).signIn(any());
    }

    @Test
    @DisplayName("로그인_username_isBlank")
    void signIn_username_isBlank() throws Exception {
        SignInRequest request = new SignInRequest("", "password");
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        post(BASE + "/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호를 필수입니다."));
        verify(authService, never()).signIn(any());
    }

    @Test
    @DisplayName("토근_재발급_쿠키_없음_401")
    void refresh_noCookie() throws Exception {
        mockMvc.perform(
                        post(BASE + "/refresh")
                )
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("토근_재발급_AccessToken을_refresh슬롯에_401")
    void refresh_accessToken_inRefresh_slot() throws Exception {
        String accessToken = JwtUtil.createAccessToken(1L, "tester");
        mockMvc.perform(
                        post(BASE + "/refresh")
                                .cookie(new Cookie("refresh", accessToken))
                )
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("토근_재발급_유효한_리프레시_토근_200")
    void refresh_valid_refreshToken() throws Exception {
        String refreshToken = JwtUtil.createRefreshToken(1L, "tester");
        mockMvc.perform(
                        post(BASE + "/refresh")
                                .cookie(new Cookie("refresh", refreshToken))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("토큰이 성공적으로 재발급되었습니다."))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("로그아웃_성공_200_쿠키_삭제")
    void logout() throws Exception {
        mockMvc.perform(delete(BASE + "/token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃되었습니다."))
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
        verifyNoInteractions(authService);
    }
}