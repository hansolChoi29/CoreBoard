package com.example.coreboard.domain.auth.controller;


import com.example.coreboard.domain.auth.service.AuthService;
import com.example.coreboard.domain.auth.dto.SignInRquest;
import com.example.coreboard.domain.auth.dto.SignUpRequest;
import com.example.coreboard.domain.auth.dto.TokenResponse;
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

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/sign-in")
    public ResponseEntity<TokenResponse> signIn(@RequestBody SignInRquest request) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }
    @PostMapping("/sign-up")
    public ResponseEntity<TokenResponse> signUp(@RequestBody SignUpRequest request) {
        if(authService.findByUsername(request.username().isPresent())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

    }
}
