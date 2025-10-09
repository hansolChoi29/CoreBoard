package com.example.coreboard.domain.auth.controller;


import com.example.coreboard.domain.auth.dto.SignUpResponse;
import com.example.coreboard.domain.auth.service.AuthService;
import com.example.coreboard.domain.auth.dto.SignUpRequest;
import com.example.coreboard.domain.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/auth")
@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 트러블 : @RequestBody 누락 - 자바 객체로 변환 (httpMessageConverter)
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(@RequestBody SignUpRequest request) {
        // 요청의 JSON데이터를 SignUpResponse 객체로 바꿔서 받음 <= @RequestBody
        // 응답은 ApiResponse<SignUpResponse> 형태로 감싸서 반환(공통 응답 포맷)
        SignUpResponse signUpResponse = authService.signup(request);
        // 서비스의 signup()메서드를 호출해서 실제 회원가입 로직 수행
        return ResponseEntity.ok(ApiResponse.ok(signUpResponse, "회원가입 성공"));
        // ApiResponse.ok는 공통 응답 포맷으로, 성공응답코드임
    }
}
