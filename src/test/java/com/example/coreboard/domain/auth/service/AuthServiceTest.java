package com.example.coreboard.domain.auth.service;

import com.example.coreboard.domain.auth.dto.SignInRequest;
import com.example.coreboard.domain.auth.dto.TokenResponse;
import com.example.coreboard.domain.common.config.PasswordEncode;
import com.example.coreboard.domain.common.util.JwtUtil;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
    AuthService authService; // 테스트용 진짜 AuthService 객체에 @Mock으로 만든 의존성들을 자동으로 주입해달라.

    @Mock
    PasswordEncode passowrdEncoder;

    @BeforeAll // 실토큰 모드
    static void setUpJwt() {
        // 테스트에서 정적 모킹을 안 쓰는 한 jwtUtil.createAccessToken() 진짜로 실행됨
        // 진짜 JJWT는 서명된 JWT문자열을 만들어내서 에러남 -> “accessToken”, “refreshToken”같은 고정 텍스트를 기대했기 때문에 무조건 실패
        // 즉, 실제 토큰을 생성하는데 가짜 문자열로 비교했기 때문에 깨진 것
        // HS256 충분히 긴 비밀키 필요
        JwtUtil.init("this-is-a-very-very-long-test-secret-key-over-32byte");
    }

    @Test
    void signUp() {

    }

    @Test
    void signIn() {
        // given : 상황세팅
        // verify : 실제 호출 확인(검증)

        // 1. given 준비단계
        // 1) DB에서 가져온 것처럼 흉내내기
        Users dummyUser = new Users(
                "tester",
                "encodedPassword",
                "email@naver.com",
                "01012341234"
        );
        // 2-1) 레포가 findByUsername 호출되면 dummyUser를 주도록 설정하고 실제 DB는 안 쓰고 모키토가 대신 이 값을 돌려주게.
        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(dummyUser));
        // 2-2) 평문 비번, 저장된 비번 비교 결과를 true로 세팅
        given(passowrdEncoder.matches(
                "password",
                "encodedPassword"
        )).willReturn(true); // 트루로 가정
        // 2-3) 이 메서드가 불리면 가짜로 돌려줘! 라는 스텁
        // 트러블 - java.lang.IllegalArgumentException: Key argument cannot be null.
        // = 메서드에 null을 넘기면 안 되는 인자(key) 가 null이었다
//        given(jwtUtil.createAccessToken(anyLong(), anyString()))
//                .willReturn("accessToken");
//        given(jwtUtil.createRefreshToken(anyLong(), anyString()))
//                .willReturn("refreshToken");

        // 2. when 실행단계
        // 1) 실제로 테스트 대상을 호출함 : result = accessToken, refreshToken 담은 응답 DTO
        TokenResponse result = authService.signIn(
                new SignInRequest("tester", "password") // 아아디/비번
        );

        // then 검증 단계: 결과가 기대한 값인지 확인하는 구간
        // 서비스의 JwtUtil.createAccessToken() / createRefreshToken()를 검증하는 코드
        // 1)어세스 토큰이 생성됐는지, 비어있지 않은지 검증
        assertNotNull(result); // 전체 응답 객체가 null 아님
        assertNotNull(result.getAccessToken()); // 액세스 토큰 null 아님 (실제로 만들어짐)
        assertFalse(result.getAccessToken().isBlank()); // 비어있지 않음 (진짜 문자열임)
        assertTrue(JwtUtil.validationToken(result.getAccessToken())); // 실제로 만들어진 JWT 문자열이 검증 로직을 통과해야 함
        // assertEquals(기대한값, 실제값)
        assertEquals("tester", JwtUtil.getUsername(result.getAccessToken())); // JWT 안의 subject값이 tester로 들어갔는지 확인, 즉
        // 이 토큰이 올바르게 만들어진 진짜 JWT인지 확인

        // 2) 리프레시 토큰도 마찬가지로 생성됐는지, 비어있지 않은지 검증
        assertNotNull(result.getRefreshToken()); // RefreshToken도 null 아님
        assertFalse(result.getRefreshToken().isBlank()); // 비어있지 않음

        // 레포가 findByUsername 한 번은 반드시 호출됐는지 확인하기
        verify(usersRepository).findByUsername("tester"); // 유저 조회
        verify(passowrdEncoder).matches("password", "encodedPassword"); // 비번 검증

        verifyNoMoreInteractions(usersRepository, passowrdEncoder);
        verifyNoMoreInteractions(usersRepository, passowrdEncoder); // 나머지 쓸데없는 호출 없었는지 확인
    }
}