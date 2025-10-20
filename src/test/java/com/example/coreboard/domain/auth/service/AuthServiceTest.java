package com.example.coreboard.domain.auth.service;

import com.example.coreboard.domain.auth.dto.SignInRequest;
import com.example.coreboard.domain.auth.dto.SignUpRequest;
import com.example.coreboard.domain.auth.dto.TokenResponse;
import com.example.coreboard.domain.common.config.EmailPhoneNumberEncode;
import com.example.coreboard.domain.common.config.PasswordEncode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.util.JwtUtil;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
    // 서비스단의 test는 controller와 어떻게 다른가?
    // 컨트롤러 - 요청응답(MockMvc), JSON, 상태, 메시지
    // 의존성: 서비스목(가짜)
    // MockMvc, status, jsonPath, verify 등

    // 서비스 - 검증(권한/분기/멱등성 규칙)
    // 의존성: 레포지토리목(가짜),DB
    // 서비스의 검증 방식은 assertEquals(), assertThrows()

    // @Mock : 유틸이 정적이라 안 먹힘
    // testImplementation 'org.mockito:mockito-inline:3.6.0' : 정적 메서드도 가짜로 바꿔서 테스트할 수 있게 해주는 도구
    // Q : 정적 메서드를 모킹하는게 안티패턴인가?

    @Mock
    UsersRepository usersRepository; // 가짜 레포

    @InjectMocks
    AuthService authService; // 테스트용 진짜 AuthService 객체에 Mock 으로 만든 의존성들을 자동으로 주입해달라.

    @Mock
    PasswordEncode passwordEncode;

    @Mock
    EmailPhoneNumberEncode emailPhoneNumberEncode;

    SignUpRequest request;

    @BeforeAll // 실토큰 모드
    static void setUpJwt() {
        // 테스트에서 정적 모킹을 안 쓰는 한 jwtUtil.createAccessToken() 진짜로 실행됨
        // 진짜 JJWT는 서명된 JWT 문자열을 만들어내서 에러남 -> “accessToken”, “refreshToken”같은 고정 텍스트를 기대했기 때문에 무조건 실패
        // 즉, 실제 토큰을 생성하는데 가짜 문자열로 비교했기 때문에 깨진 것
        // HS256 충분히 긴 비밀키 필요
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
    void signUp() {
        // given
        // 이런 일이 일어날 때, 이렇게 하라고 미리 설정
        given(usersRepository.existsByUsername("tester")).willReturn(false); // 원래는 DB에 tester라는 아이디가 존재하는지 확인하는
        // 용도인데, 목이니깐 가짜로 행동해야 됨 false 반환 (중복 아님)
        given(passwordEncode.encrypt("password")).willReturn("encodedPassword"); // 암호화된 비번 반환
        given(emailPhoneNumberEncode.encrypt("email@naver.com")).willReturn("encEmail"); // 암호화된 이메일로 반환
        given(emailPhoneNumberEncode.encrypt("01012341234")).willReturn("encPhoneNumber"); // 암호화된 번호 반환

        // stup : 가짜 객체(Mock)에 미리 짜둔 응답 데이터 준비
        Users saved = new Users(
                "tester",
                "encodedPassword",
                "encEmail",
                "encPhoneNumber"
        ); // given에서 설정해준대로 Users에 담기

        // when
        given(usersRepository.save(any(Users.class))).willReturn(saved); // 파라미터로 들어온 Users 객체가 무엇이든 상관없이
        // usersRepository.save 호출이 발생했을 때 saved를 리턴해라
        Users result = authService.signUp(request); // result 안에 saved 객체가 그대로 들어감

        // then(검증) : 실제 실행 결과
        assertNotNull(result); // result는 null이 아니어야 한다.
        assertEquals("tester", result.getUsername()); // result 안에 들어있는 username값이 tester와 같아야 한다.
        // 진짜 암호화가 된 결과를 비교하는 게 아니라, 가짜 암호화 결과를 미리 정해두고 그 값이 들어갔는지 확인하는 테스트
        assertEquals("encodedPassword", result.getPassword()); // result 안에 들어있는 password값이 encodedPassword와 같아야 한다.
        assertEquals("encEmail", result.getEmail()); // result 안에 들어있는 email값이 encEmail와 같아야 한다.
        assertEquals("encPhoneNumber", result.getPhoneNumber()); // result 안에 들어있는 phoneNumber값이 encPhoneNumber와 같아야 한다.

        verify(usersRepository).existsByUsername("tester");
        // 암호화 검증
        verify(passwordEncode).encrypt("password");
        verify(emailPhoneNumberEncode).encrypt("email@naver.com");
        verify(emailPhoneNumberEncode).encrypt("01012341234");
    }

    @Test
    void signIn() {
        // given : 상황세팅
        // verify : 실제 호출 확인(검증)

        // given 준비단계
        // DB 에서 가져온 것처럼 흉내내기
        Users dummyUser = new Users(
                "tester",
                "encodedPassword",
                "email@naver.com",
                "01012341234"
        );
        // 레포가 findByUsername 호출되면 dummyUser를 주도록 설정하고 실제 DB는 안 쓰고 모키토가 대신 이 값을 돌려주게.
        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(dummyUser));
        // 평문 비번, 저장된 비번 비교 결과를 true로 세팅
        given(passwordEncode.matches("password", "encodedPassword")).willReturn(true); // 트루로 가정
        // 이 메서드가 불리면 가짜로 돌려줘! 라는 스텁
        // 트러블 - java.lang.IllegalArgumentException: Key argument cannot be null.
        // = 메서드에 null을 넘기면 안 되는 인자(key) 가 null 이었다
//        given(jwtUtil.createAccessToken(anyLong(), anyString()))
//                .willReturn("accessToken");
//        given(jwtUtil.createRefreshToken(anyLong(), anyString()))
//                .willReturn("refreshToken");

        // when 실행단계
        // 실제로 테스트 대상을 호출함 : result = accessToken, refreshToken 담은 응답 DTO
        TokenResponse result = authService.signIn(
                new SignInRequest("tester", "password") // 아아디/비번
        );

        // then 검증 단계: 결과가 기대한 값인지 확인하는 구간
        // 서비스의 JwtUtil.createAccessToken() / createRefreshToken()를 검증하는 코드
        // 어세스 토큰이 생성됐는지, 비어있지 않은지 검증
        assertNotNull(result); // 전체 응답 객체가 null 아님
        assertNotNull(result.getAccessToken()); // 액세스 토큰 null 아님 (실제로 만들어짐)
        assertFalse(result.getAccessToken().isBlank()); // 비어있지 않음 (진짜 문자열임)
        assertTrue(JwtUtil.validationToken(result.getAccessToken())); // 실제로 만들어진 JWT 문자열이 검증 로직을 통과해야 함
        // assertEquals(기대한값, 실제값)
        assertEquals("tester", JwtUtil.getUsername(result.getAccessToken())); // JWT 안의 subject 값이 tester로 들어갔는지 확인, 즉
        // 이 토큰이 올바르게 만들어진 진짜 JWT 인지 확인

        // 리프레시 토큰도 마찬가지로 생성됐는지, 비어있지 않은지 검증
        assertNotNull(result.getRefreshToken()); // RefreshToken도 null 아님
        assertFalse(result.getRefreshToken().isBlank()); // 비어있지 않음

        // 레포가 findByUsername 한 번은 반드시 호출됐는지 확인하기
        verify(usersRepository).findByUsername("tester"); // 유저 조회
        verify(passwordEncode).matches(eq("password"), eq("encodedPassword")); // 비번 검증

        verifyNoMoreInteractions(usersRepository, passwordEncode);
        verifyNoMoreInteractions(usersRepository, passwordEncode); // 나머지 쓸데없는 호출 없었는지 확인
    }

    @Test
    void signIn_빈값_BadRequest() {
        // username blank
        AuthErrorException usernameIsBlank = assertThrows(AuthErrorException.class,
                () -> authService.signIn(new SignInRequest(" ", "password")));
        assertEquals(400, usernameIsBlank.getStatus());

        // password blank
        AuthErrorException passwordIsBlank = assertThrows(AuthErrorException.class,
                () -> authService.signIn(new SignInRequest("tester", " ")));
        assertEquals(400, passwordIsBlank.getStatus());

        AuthErrorException usernameAndPasswordIsBlank = assertThrows(AuthErrorException.class,
                () -> authService.signIn(new SignInRequest(" ", " ")));
        assertEquals(400, usernameAndPasswordIsBlank.getStatus());
    }
}