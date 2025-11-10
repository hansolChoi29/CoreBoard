package com.example.coreboard.domain.auth.controller;


import com.example.coreboard.domain.auth.dto.*;
import com.example.coreboard.domain.auth.dto.command.SignInCommand;
import com.example.coreboard.domain.auth.dto.command.SignUpCommand;
import com.example.coreboard.domain.auth.dto.request.SignInRequest;
import com.example.coreboard.domain.auth.dto.request.SignUpRequest;
import com.example.coreboard.domain.auth.dto.response.SignUpResponse;
import com.example.coreboard.domain.auth.dto.response.TokenResponse;
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

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(@RequestBody SignUpRequest request) {
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
        return ResponseEntity.ok(ApiResponse.ok(response, "회원가입 성공"));
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

        TokenResponse response = new TokenResponse(
                out.getAccessToken(),
                out.getRefreshToken()
        );

        return ResponseEntity.ok(ApiResponse.ok(response, "로그인 성공!"));
    }
}
