package com.example.coreboard.domain.auth.controller;


import com.example.coreboard.domain.auth.dto.*;
import com.example.coreboard.domain.auth.service.AuthService;
import com.example.coreboard.domain.common.response.ApiResponse;
import com.example.coreboard.domain.common.validation.AuthValidation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/auth")
@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(
            AuthService authService
    ) {
        this.authService = authService;
    }

    // 트러블 : @RequestBody 누락 - 자바 객체로 변환 (httpMessageConverter)
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(@RequestBody SignUpRequest request) {
        // 요청의 JSON데이터를 SignUpResponse 객체로 바꿔서 받음 <= @RequestBody
        // 응답은 ApiResponse<SignUpResponse> 형태로 감싸서 반환(공통 응답 포맷)
        AuthValidation.signUpValidation(request);

        SignUpCommand users = new SignUpCommand(
                request.getUsername(),
                request.getPassword(),
                request.getConfirmPassword(),
                request.getEmail(),
                request.getPhoneNumber()
        );

        SignUpDto out = authService.signUp(users);

        SignUpResponse response = new SignUpResponse(
                out.getUsername()
        );
        // 클라이언트->컨트롤러 : request
        // 컨트롤러->서비스 : command
        // 서비스->컨트롤러 : result
        // 컨트롤러->클라이언트 : response

        // 서비스의 signup()메서드를 호출해서 실제 회원가입 로직 수행
        return ResponseEntity.ok(ApiResponse.ok(response, "회원가입 성공"));
        // ApiResponse.ok는 공통 응답 포맷으로, 성공응답코드임
    }

    @PostMapping("/token")
    public ResponseEntity<ApiResponse<TokenResponse>> signIn(
            @RequestBody SignInRequest request
    ) {

        AuthValidation.signInValidation(request);

        SignInCommand users = new SignInCommand(
                request.getUsername(),
                request.getPassword()
        );

        TokenDto out = authService.signIn(users);
        // 클라이언트->컨트롤러 : request
        // 컨트롤러->서비스 : command
        // 서비스->컨트롤러 : result
        // 컨트롤러->클라이언트 : response
        TokenResponse response = new TokenResponse(
                out.getAccessToken(),
                out.getRefreshToken()
        );

        return ResponseEntity.ok(ApiResponse.ok(response, "로그인 성공!")); // 도메인 로그인 토큰 넣어야 해서 응답바디에 반환되게 함
    }
}
