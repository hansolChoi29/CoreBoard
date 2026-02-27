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

        ResponseCookie refreshCookies = ResponseCookie.from("refresh", out.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/auth/refresh")
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
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        if (refreshToken == null || !JwtUtil.validationRefreshToken(refreshToken)) {
            throw new AuthErrorException(AuthErrorCode.UNAUTHORIZED);
        }
        String username = JwtUtil.getUsername(refreshToken);
        Long userId = JwtUtil.getUserId(refreshToken);
        String newAccessToken = JwtUtil.createAccessToken(userId, username);
        return ResponseEntity.ok(ApiResponse.ok(new TokenResponse(newAccessToken), "토큰이 성공적으로 재발급되었습니다."));
    }

    @DeleteMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> logout() {
        ResponseCookie deleteCookie = ResponseCookie.from("refresh", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/auth/refresh")
                .maxAge(0)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(ApiResponse.ok(null, "로그아웃되었습니다."));
    }
}
