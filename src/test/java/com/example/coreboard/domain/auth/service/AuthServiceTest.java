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
import com.example.coreboard.domain.users.entity.UserRole;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
    PasswordManager passwordManager;

    @Mock
    EmailPhoneNumberManager emailPhoneNumberManager;

    SignUpRequest request;

    @BeforeAll
    static void setUpJwt() {
        JwtUtil.init("this-is-a-very-very-long-test-secret-key-over-32byte");
    }

    @BeforeEach
    void setUp() {
        request = new SignUpRequest(
                "tester",
                "nickname",
                "password",
                "password",
                "email@naver.com",
                "01012341234",
                UserRole.USER);
    }

    @Test
    @DisplayName("회원가입_성공")
    void signUp() {
        given(usersRepository.existsByUsername("tester")).willReturn(false);
        given(passwordManager.encrypt("password")).willReturn("encodedPassword");
        given(emailPhoneNumberManager.encrypt("email@naver.com")).willReturn("encEmail");
        given(emailPhoneNumberManager.encrypt("01012341234")).willReturn("encPhoneNumber");
        Users savedUser = new Users(
                "tester",
                "nickname",
                "encodedPassword",
                "encEmail",
                "encPhoneNumber",
                UserRole.USER);
        given(usersRepository.save(any(Users.class))).willReturn(savedUser);
        SignUpCommand command = new SignUpCommand(
                "tester",
                "nickname",
                "password",
                "password",
                "email@naver.com",
                "01012341234");

        SignUpDto result = authService.signUp(command);

        assertNotNull(result);
        assertEquals("tester", result.username());

        verify(usersRepository).existsByUsername("tester");

        verify(passwordManager).encrypt("password");
        verify(emailPhoneNumberManager).encrypt("email@naver.com");
        verify(emailPhoneNumberManager).encrypt("01012341234");
    }

    @Test
    @DisplayName("회원가입_성공_이메일과_휴대폰번호는_암호화되어_회원엔티티에_저장")
    void signUpSavesEncryptedEmailAndPhoneNumber() {
        SignUpCommand command = new SignUpCommand(
                "tester",
                "nickname",
                "password123!",
                "password123!",
                "tester@example.com",
                "01012341234"
        );

        given(usersRepository.existsByUsername("tester")).willReturn(false);
        given(passwordManager.encrypt("password123!")).willReturn("encodedPassword");
        given(emailPhoneNumberManager.encrypt("tester@example.com")).willReturn("encryptedEmail");
        given(emailPhoneNumberManager.encrypt("01012341234")).willReturn("encryptedPhoneNumber");

        authService.signUp(command);

        ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
        verify(usersRepository).save(captor.capture());

        Users savedUser = captor.getValue();

        assertEquals("tester", savedUser.getUsername());
        assertEquals("nickname", savedUser.getNickname());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals("encryptedEmail", savedUser.getEmail());
        assertEquals("encryptedPhoneNumber", savedUser.getPhoneNumber());

        verify(usersRepository).existsByUsername("tester");
        verify(passwordManager).encrypt("password123!");
        verify(emailPhoneNumberManager).encrypt("tester@example.com");
        verify(emailPhoneNumberManager).encrypt("01012341234");
        verify(usersRepository).save(any(Users.class));
    }

    @Test
    @DisplayName("회원가입_존재_유저_409")
    void signUp_IsConflict() {
        given(usersRepository.existsByUsername("tester")).willReturn(true);

        AuthErrorException usernameIsConflict = assertThrows(AuthErrorException.class,
                () -> authService.signUp(new SignUpCommand(
                        "tester",
                        "nickname",
                        "password",
                        "password",
                        "email@naver.com",
                        "01012341234")));
        assertEquals(HttpStatus.CONFLICT, usernameIsConflict.getStatus());
        verify(usersRepository).existsByUsername("tester");
    }

    @Test
    @DisplayName("로그인_성공")
    void signIn() {

        Users dummyUser = new Users(
                "tester",
                "nickname",
                "encodedPassword",
                "email@naver.com",
                "01012341234",
                UserRole.USER);
        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(dummyUser));
        given(passwordManager.matches("password", "encodedPassword")).willReturn(true);

        TokenDto result = authService.signIn(
                new SignInCommand("tester", "password"));

        assertNotNull(result);
        assertNotNull(result.accessToken());
        assertFalse(result.accessToken().isBlank());
        assertTrue(JwtUtil.validationToken(result.accessToken()));

        assertEquals("tester", JwtUtil.getUsername(result.accessToken()));
        assertNotNull(result.refreshToken());
        assertFalse(result.refreshToken().isBlank());

        verify(usersRepository).findByUsername("tester");
        verify(passwordManager).matches(eq("password"), eq("encodedPassword"));

        verifyNoMoreInteractions(usersRepository, passwordManager);
        verifyNoMoreInteractions(usersRepository, passwordManager);
    }

    @Test
    @DisplayName("로그인_존재하지_않는_사용자")
    void signIn_isNotFound() {
        given(usersRepository.findByUsername("tester")).willReturn(Optional.empty());

        AuthErrorException usernameNotFound = assertThrows(
                AuthErrorException.class,
                () -> authService.signIn(new SignInCommand(
                        "tester",
                        "password")));
        assertEquals(HttpStatus.NOT_FOUND, usernameNotFound.getStatus());
        verify(usersRepository).findByUsername("tester");
    }

    @Test
    @DisplayName("로그인_비번_불일치_401")
    void signIn_isUnAuthorized() {
        Users dummy = new Users(
                "tester",
                "nickname",
                "encodedPassword",
                "email@naver.com",
                "01012341234",
                UserRole.USER);

        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(dummy));
        given(passwordManager.matches("password", "encodedPassword")).willReturn(false);

        AuthErrorException passwordUnAuthorized = assertThrows(
                AuthErrorException.class,
                () -> authService.signIn(new SignInCommand(
                        "tester",
                        "password")));

        assertEquals(HttpStatus.UNAUTHORIZED, passwordUnAuthorized.getStatus());

        verify(usersRepository).findByUsername("tester");
        verify(passwordManager).matches("password", "encodedPassword");
    }
}