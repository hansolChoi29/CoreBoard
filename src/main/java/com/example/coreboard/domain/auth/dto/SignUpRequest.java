package com.example.coreboard.domain.auth.dto;

public class SignUpRequest {
    public String username;
    String password;

    public SignUpRequest(String username, String password) {
        this.username=username;
        this.password=password;
    }

    public String username() {
        return username;
    }
}
