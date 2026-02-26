package com.example.coreboard.domain.auth.controller;

import com.example.coreboard.domain.auth.dto.*;
import com.example.coreboard.domain.auth.dto.command.SignInCommand;
import com.example.coreboard.domain.auth.dto.command.SignUpCommand;
import com.example.coreboard.domain.auth.dto.request.RefreshRequest;
import com.example.coreboard.domain.auth.dto.request.SignInRequest;
import com.example.coreboard.domain.auth.dto.request.SignUpRequest;
import com.example.coreboard.domain.auth.dto.response.SignUpResponse;
import com.example.coreboard.domain.auth.dto.response.TokenResponse;
import com.example.coreboard.domain.auth.service.AuthService;
import com.example.coreboard.domain.common.response.ApiResponse;
import com.example.coreboard.domain.common.validation.AuthValidation;
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
         * TODO : /auth/refresh : 쿠키에서 refresh 토큰을 꺼내 validationRefreshTokne()으로 검정, 통과하면 새 AccessTokne 발급
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
    ){

    }
}
