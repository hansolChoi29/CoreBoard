package com.example.coreboard.domain.auth.controller;

import com.example.coreboard.domain.auth.dto.*;
import com.example.coreboard.domain.auth.dto.command.SignInCommand;
import com.example.coreboard.domain.auth.dto.command.SignUpCommand;
import com.example.coreboard.domain.auth.dto.request.SignInRequest;
import com.example.coreboard.domain.auth.dto.request.SignUpRequest;
import com.example.coreboard.domain.auth.dto.response.SignUpResponse;
import com.example.coreboard.domain.auth.dto.response.TokenResponse;
import com.example.coreboard.domain.auth.service.AuthService;
import com.example.coreboard.domain.common.exception.auth.AuthErrorCode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.response.ApiResponse;
import com.example.coreboard.domain.common.util.JwtUtil;
import com.example.coreboard.domain.common.validation.AuthValidation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/auth")
@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(
            AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(@RequestBody SignUpRequest request) {
        AuthValidation.signUpValidation(request);

        SignUpCommand users = new SignUpCommand(
                request.username(),
                request.password(),
                request.confirmPassword(),
                request.email(),
                request.phoneNumber());

        SignUpDto out = authService.signUp(users);

        SignUpResponse response = new SignUpResponse(out.getUsername());

        return ResponseEntity.ok(ApiResponse.ok(response, "회원가입 성공"));
    }

    @PostMapping("/token")
    public ResponseEntity<ApiResponse<TokenResponse>> signIn(
            @RequestBody SignInRequest request) {
        AuthValidation.signInValidation(request);

        SignInCommand users = new SignInCommand(
                request.username(),
                request.password());
        TokenDto out = authService.signIn(users);

        /*
         * /auth/token 시 refreshToken을 path쿠키로 설정해 놓았는데,
         * 정작 /auth/refresh 엔드포인트가 없음
         * 또한 /auth/logout 엔드포인트도 없음을 발견
         * 즉, 쿠키에 path 설정까지 해놓고 그 경로에 아무것도 없는 미완성인 것
         */

        /*
         * TODO : /auth/logout : 로그인할 때 설정한 쿠키랑 완전 동일한 속성인 httpOnly, secure, sameSite, path로 maxAge=0짜리 쿠키를 덮어쓰고 브라우저가 maxAge가 0이면 즉시 해당 쿠키 삭제하는 동작
         */
        ResponseCookie refreshCookies = ResponseCookie.from("refresh", out.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/auth/refresh") // 이 경로에 엔드포인트가 없음을 발견함
                .maxAge(7 * 24 * 60 * 60)
                .build();
        TokenResponse response = new TokenResponse(out.accessToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookies.toString())
                .body(ApiResponse.ok(response, "로그인 성공!"));
    }

    @PostMapping("/refresh")
    ResponseEntity<ApiResponse<TokenResponse>> refresh(
            HttpServletRequest request
    ) {
        // 1) refresh 담을 변수 선언
        String refreshToken = null;
        // 2) HTTP 요청에는 쿠키가 여러 개 담겨 올 수 있음 그래서 쿠키 목록을 하나씩 돌면서 이름이 refresh인 걸 찾아서 값을 꺼내야 함
        // 2-1) getCookie()가 null 인 경우도 있는데 요청에 쿠키가 아예 없으면 null을 반환함 그래서 null 체크를 먼저 할 것
        if (request.getCookies() != null) {
            for (Cookie cookie : (Cookie[]) request.getCookies()) {
                if ("refresh".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        // 3) refreshToken == null -> 쿠키 자체가 없음 || 쿠키는 있는데 만료
        if (refreshToken == null || !JwtUtil.validationRefreshToken(refreshToken)) {
            throw new AuthErrorException(AuthErrorCode.UNAUTHORIZED);
        }
        // 4) 검증 통과한 refreshToken에서 username이랑 userId를 꺼내서 AccessToken을 만듦
        // 4-1) DB 조회 x, 토큰 안에 있는 정보만으로 만듦
        String username = JwtUtil.getUsername(refreshToken);
        Long userId = JwtUtil.getUserId(refreshToken);
        String newAccessToken = JwtUtil.createAccessToken(userId, username);
        // 5) 새로 만든 AccessToken을 응답으로 돌려줄 것
        return ResponseEntity.ok(ApiResponse.ok(new TokenResponse(newAccessToken), "토큰이 성공적으로 재발급되었습니다."));
    }
}
