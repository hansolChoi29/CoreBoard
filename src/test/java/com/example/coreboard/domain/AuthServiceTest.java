package com.example.coreboard.domain;

import com.example.coreboard.domain.auth.service.AuthService;
import com.example.coreboard.domain.common.config.EmailPhoneNumberEncode;
import com.example.coreboard.domain.common.config.PasswordEncode;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.Test;

public class AuthServiceTest {
    //    public static void main(String[] args){
    @Test
    void SignUpTest() {
        PasswordEncode passwordEncode = new PasswordEncode();
        UsersRepository userReposiroty = null;
        EmailPhoneNumberEncode emailPhoneNumberEncode=null;
        AuthService authService = new AuthService(passwordEncode, userReposiroty, emailPhoneNumberEncode);
        System.out.println("객체 생성 여기임" + authService);
    }
}
