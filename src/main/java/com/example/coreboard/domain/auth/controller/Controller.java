package com.example.coreboard.domain.auth.controller;


import com.example.coreboard.domain.auth.Repository.AuthRepository;
import com.example.coreboard.domain.auth.dto.SignInRquest;
import com.example.coreboard.domain.auth.dto.TokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/auth")
public class Controller {

 private final AuthRepository authRepository;

    public Controller(AuthRepository authRepository) {
        this.authRepository=authRepository;
    }

    //
    @PostMapping("/sign-in")
        public ResponseEntity<TokenResponse> signIn(@RequestBody SignInRquest request){
           return ResponseEntity.status(HttpStatus.OK).build();
        }
}
