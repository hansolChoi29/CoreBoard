package com.example.coreboard.domain.auth.dto;

public class SignInRequest {
    String username;
    String password;

    public SignInRequest(String username,String password){
        this.username=username;
        this.password=password;
    }

    public String getUsername(){
        return username;
    }
    public String getPassword(){
        return password;
    }
}
