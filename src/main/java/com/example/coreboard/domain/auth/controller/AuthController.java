package com.example.coreboard.domain.auth.controller;


import com.example.coreboard.domain.auth.dto.SignInRequest;
import com.example.coreboard.domain.auth.dto.SignUpResponse;
import com.example.coreboard.domain.auth.dto.TokenResponse;
import com.example.coreboard.domain.auth.service.AuthService;
import com.example.coreboard.domain.auth.dto.SignUpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;

   public AuthController(AuthService authService){
       this.authService=authService;
   }

   //ResponseEntity가 뭔데?
        // HTTP응답 통째로 담는 상자.
        // 상태코드, 헤더(Content-Type), 바디(JSON)

    @PostMapping("/sign-in")
    public ResponseEntity<TokenResponse> signIn(@RequestBody SignInRequest signinRequest) {
       return ResponseEntity.status(HttpStatus.CREATED).body(authService.signin(signinRequest));
   }

    @PostMapping("/sign-up")
    public ResponseEntity<SignUpResponse> signUp( @RequestBody SignUpRequest  signupRequset){
        return ResponseEntity.ok(authService.signup(signupRequset));
    }
}
