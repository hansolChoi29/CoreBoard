package com.example.coreboard.domain.auth.controller;


import com.example.coreboard.domain.auth.dto.SignUpResponse;
import com.example.coreboard.domain.auth.service.AuthService;
import com.example.coreboard.domain.auth.dto.SignUpRequest;
import com.example.coreboard.domain.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/auth")
@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    //ResponseEntity가 뭔데?
    // HTTP응답 통째로 담는 상자.
    // 상태코드, 헤더(Content-Type), 바디(JSON)

    // 201: 새로운 리소스를 만들었다는 의미
    // 로그인은 200

    //    @PostMapping("/sign-in")
//    public ResponseEntity<TokenResponse> signIn(@RequestBody SignInRequest signinRequest) {
//        TokenResponse token = authService.signin(signinRequest).getBody(); //로그인 처리 후 토큰 발급
//        return ResponseEntity.ok(token); // 토큰 응답
//    }
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(SignUpRequest request) {
        SignUpResponse signUpResponse = authService.signup(request);
        return ResponseEntity.ok(ApiResponse.ok(signUpResponse, "회원가입 성공"));
    }
}
