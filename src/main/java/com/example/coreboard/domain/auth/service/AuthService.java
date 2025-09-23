package com.example.coreboard.domain.auth.service;

import com.example.coreboard.domain.auth.dto.SignUpRequest;
import com.example.coreboard.domain.auth.dto.SignUpResponse;
import com.example.coreboard.domain.auth.repository.AuthRepository;
import com.example.coreboard.domain.config.PasswordEncode;
import com.example.coreboard.domain.users.entity.Users;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthRepository authRepository;

    private final PasswordEncode passwordEncode;

    public AuthService(AuthRepository authRepository, PasswordEncode passwordEncode){
        this.authRepository=authRepository;
        this.passwordEncode=passwordEncode;
    }


   @Transactional
    public SignUpResponse signup(SignUpRequest signupRequest){
        // 트러블: getter인데 파라미터를 넣어서 에러났었음
       //signupRequest.getUsername(username)
        if(authRepository.existsByUsername(signupRequest.getUsername())){
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        String encodePassword = passwordEncode.encrypt(signupRequest.getPassword());

        Users newUser=new Users(
                signupRequest.getUsername(),
                encodePassword
        );
        authRepository.save(newUser);

        return new SignUpResponse(newUser.getUsername(),"가입 성공!");


   }

}
