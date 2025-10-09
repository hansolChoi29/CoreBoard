package com.example.coreboard.domain;

import com.example.coreboard.domain.auth.service.AuthService;
import com.example.coreboard.domain.common.config.PasswordEncode;
import com.example.coreboard.domain.users.repository.UsersRepository;

public class AuthServiceTest {
    public static void main(String[] args){
        PasswordEncode passwordEncode= new PasswordEncode();
        UsersRepository userReposiroty = null;
        AuthService authService = new AuthService(passwordEncode, userReposiroty);
        System.out.println("객체 생성 여기임"+authService);
    }
}
