package com.example.coreboard.domain.auth.service;

import com.example.coreboard.domain.auth.dto.*;
import com.example.coreboard.domain.auth.dto.command.SignInCommand;
import com.example.coreboard.domain.auth.dto.command.SignUpCommand;
import com.example.coreboard.domain.auth.dto.request.SignInRequest;
import com.example.coreboard.domain.auth.dto.request.SignUpRequest;
import com.example.coreboard.domain.common.config.EmailPhoneNumberManager;
import com.example.coreboard.domain.common.config.PasswordManager;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.util.JwtUtil;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    UsersRepository usersRepository;

    @InjectMocks
    AuthService authService;

    @Mock
    PasswordManager passwordEncode;

    @Mock
    EmailPhoneNumberManager emailPhoneNumberEncode;

    SignUpRequest request;

    @BeforeAll
    static void setUpJwt() {
        JwtUtil.init("this-is-a-very-very-long-test-secret-key-over-32byte");
    }

    @BeforeEach
    void setUp() {
        request = new SignUpRequest(
                "tester",
                "password",
                "password",
                "email@naver.com",
                "01012341234"
        );
    }

    @Test
    @DisplayName("회원가입_성공")
    void signUp() {

        given(usersRepository.existsByUsername("tester")).willReturn(false);
        given(passwordEncode.encrypt("password")).willReturn("encodedPassword");
        given(emailPhoneNumberEncode.encrypt("email@naver.com")).willReturn("encEmail");
        given(emailPhoneNumberEncode.encrypt("01012341234")).willReturn("encPhoneNumber");
        Users savedUser = new Users(
                "tester",
                "encodedPassword",
                "encEmail",
                "encPhoneNumber"
        );
        given(usersRepository.save(any(Users.class))).willReturn(savedUser);
        SignUpCommand command = new SignUpCommand(
                "tester",
                "password",
                "password",
                "email@naver.com",
                "01012341234"
        );

        SignUpDto result = authService.signUp(command);

        assertNotNull(result);
        assertEquals("tester", result.getUsername());

        Users user = new Users("tester", "encodedPassword", "encEmail", "encPhoneNumber");
        assertEquals("encEmail", user.getEmail());
        assertEquals("encPhoneNumber", user.getPhoneNumber());
        result.setUsername("renamedUser");
        assertEquals("renamedUser", result.getUsername());
        verify(usersRepository).existsByUsername("tester");

        verify(passwordEncode).encrypt("password");
        verify(emailPhoneNumberEncode).encrypt("email@naver.com");
        verify(emailPhoneNumberEncode).encrypt("01012341234");
    }

    @Test
    @DisplayName("회원가입_비밀번호_불일치_400")
    void signUp_IsMisMatch() {
        AuthErrorException passwordMismatch = assertThrows(
                AuthErrorException.class,
                () -> authService.signUp(new SignUpCommand(
                        "tester",
                        "password",
                        "pa",
                        "email@naver.com",
                        "01012341234"
                ))
        );
        assertEquals(400, passwordMismatch.getStatus());
    }

    @Test
    @DisplayName("회원가입_비밀번호_null_400")
    void signUp_IsPasswordNull() {
        AuthErrorException passwordNull = assertThrows(
                AuthErrorException.class,
                () -> authService.signUp(new SignUpCommand(
                        "tester",
                        null,
                        "password",
                        "email@naver.com",
                        "01012341234"
                ))
        );
        assertEquals(400, passwordNull.getStatus());
    }

    @Test
    @DisplayName("회원가입_비밀번호확인_null_400")
    void signUp_IsConfirmPasswordNull() {
        AuthErrorException confirmPasswordNull = assertThrows(
                AuthErrorException.class,
                () -> authService.signUp(new SignUpCommand(
                        "tester",
                        "password",
                        null,
                        "email@naver.com",
                        "01012341234"
                ))
        );
        assertEquals(400, confirmPasswordNull.getStatus());
    }

    @Test
    @DisplayName("회원가입_존재_유저_409")
    void signUp_IsConflict() {
        given(usersRepository.existsByUsername("tester")).willReturn(true);

        AuthErrorException usernameIsConflict = assertThrows(AuthErrorException.class,
                () -> authService.signUp(new SignUpCommand(
                        "tester",
                        "password",
                        "password",
                        "email@naver.com",
                        "01012341234"
                )));
        assertEquals(409, usernameIsConflict.getStatus());
        verify(usersRepository).existsByUsername("tester");
    }

    @Test
    @DisplayName("로그인_성공")
    void signIn() {

        Users dummyUser = new Users(
                "tester",
                "encodedPassword",
                "email@naver.com",
                "01012341234"
        );
        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(dummyUser));
        given(passwordEncode.matches("password", "encodedPassword")).willReturn(true);

        TokenDto result = authService.signIn(
                new SignInCommand("tester", "password")
        );

        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertFalse(result.getAccessToken().isBlank());
        assertTrue(JwtUtil.validationToken(result.getAccessToken()));

        assertEquals("tester", JwtUtil.getUsername(result.getAccessToken()));
        assertNotNull(result.getRefreshToken());
        assertFalse(result.getRefreshToken().isBlank());

        verify(usersRepository).findByUsername("tester");
        verify(passwordEncode).matches(eq("password"), eq("encodedPassword"));

        verifyNoMoreInteractions(usersRepository, passwordEncode);
        verifyNoMoreInteractions(usersRepository, passwordEncode);
    }

    @Test
    @DisplayName("로그인_존재하지_않는_사용자")
    void signIn_isNotFound() {
        given(usersRepository.findByUsername("tester")).willReturn(Optional.empty());

        AuthErrorException usernameNotFound = assertThrows(
                AuthErrorException.class,
                () -> authService.signIn(new SignInCommand(
                        "tester",
                        "password"
                )));
        assertEquals(404, usernameNotFound.getStatus());
        verify(usersRepository).findByUsername("tester");
    }

    @Test
    @DisplayName("로그인_비번_불일치_401")
    void signIn_isUnAuthorized() {
        Users dummy = new Users(
                "tester",
                "encodedPassword",
                "email@naver.com",
                "01012341234"
        );

        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(dummy));
        given(passwordEncode.matches("password", "encodedPassword")).willReturn(false);

        AuthErrorException passwordUnAuthorized = assertThrows(
                AuthErrorException.class,
                () -> authService.signIn(new SignInCommand(
                        "tester",
                        "password"
                )));

        assertEquals(401, passwordUnAuthorized.getStatus());

        verify(usersRepository).findByUsername("tester");
        verify(passwordEncode).matches("password", "encodedPassword");
    }

    @Test
    @DisplayName("로그인_DTO_옮겨갈_때")
    void signIn_flow_mapping() {
        SignInRequest request = new SignInRequest("tester", "123ps");

        request.setUsername("tester");
        request.setPassword("1234ps");

        SignInCommand command = new SignInCommand(
                request.getUsername(),
                request.getPassword()
        );

        command.setUsername(command.getUsername());
        command.setPassword(command.getPassword());

        assertEquals("tester", command.getUsername());
        assertEquals("1234ps", command.getPassword());
    }
}