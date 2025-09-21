package com.example.coreboard.domain.auth.service;

import com.example.coreboard.domain.auth.repository.AuthRepository;

public class AuthService {
    private final AuthRepository authRepository;

    public AuthService(AuthRepository authRepository){
        this.authRepository=authRepository;
    }
    public void findByUsername(String username){
        if(username!=null){
             authRepository.findByUsername(username);
        }
        else{
            throw new IllegalArgumentException();
        }
    }
}
