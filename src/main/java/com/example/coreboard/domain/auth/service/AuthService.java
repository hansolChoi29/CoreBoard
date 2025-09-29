package com.example.coreboard.domain.auth.service;

import com.example.coreboard.domain.auth.dto.SignInRequest;
import com.example.coreboard.domain.auth.dto.SignUpRequest;
import com.example.coreboard.domain.auth.dto.SignUpResponse;
import com.example.coreboard.domain.auth.dto.TokenResponse;
import com.example.coreboard.domain.auth.repository.AuthRepository;
import com.example.coreboard.domain.common.config.PasswordEncode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.util.JwtUtil;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import static com.example.coreboard.domain.common.exception.auth.AuthErrorCode.*;


@Service
public class AuthService {
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final PasswordEncode passwordEncoder;

    public AuthService(AuthRepository authRepository, PasswordEncode passwordEncoder, UserRepository userRepository) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Transactional
    public SignUpResponse signup(SignUpRequest signupRequest) {
        // 트러블: getter인데 파라미터를 넣어서 에러났었음
        //signupRequest.getUsername(username)
        if (authRepository.existsByUsername(signupRequest.getUsername())) {
            throw new AuthErrorException(CONFLICT);
        }
        String encodePassword = passwordEncoder.encrypt(signupRequest.getPassword());

        Users newUser = new Users(
                signupRequest.getUsername(),
                encodePassword
        );
        authRepository.save(newUser);
        return new SignUpResponse(newUser.getUsername(), "가입 성공!");
    }

    // 검증을 한 번 더 하는 이유
    // 유틸은 단순히 JWT 구조/서명/만료 검증
    // 서비스는 Claim값 검증, DB조회/사용자 체크

    // 서비스 로그인 
    // 1) 사용자 인증 - DB 조회 검증
    // 2) JWT 발급 - 로그인 성공 시 유틸 호출
    // 3) 응답 DTO 반환 - accessToken

    @Transactional
    public ResponseEntity<TokenResponse> signin(@RequestBody SignInRequest signinRequest) {
        // 사용자 입력 정보 추출
        String username = signinRequest.getUsername(); //  사용자 아이디
        String inputPassword = signinRequest.getPassword(); // 사용자 비밀번호

        // DB에서 사용자 조회
        Users user = userRepository.findByUsername(username) //DB에 접근하여 아이디 조회
                .orElseThrow(() -> new AuthErrorException(NOT_FOUND)); // 값이 없을 경우 에러 던짐, 있다면 값 반환
        // 비밀번호 검증
        if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
            // matches : PasswordEncoder객체가 제공하는 메서드여야 함
            throw new AuthErrorException(BAD_REQUEST);
        } else {
            Long userId = user.getUserId(); // DB에서 사용자 ID 조회
            String accessToken = JwtUtil.createAccessToken(userId, username); // 엑세스 토큰 발급
            String refreshToken = JwtUtil.createRefreshToken(userId, username); // 엑세스 토큰 발급

            // put은 문법상 맞지 않음
            // tokenResponse.put("accessToken",accessToken);
            // tokenResponse.put("refreshToken",refreshToken);

            TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken); // 토큰 세터로 주입
            return ResponseEntity.ok().body(tokenResponse); // 바디에 accessToken, refreshToken응답
        }
    }
}
