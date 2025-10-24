package com.example.coreboard.domain.auth.dto;

public class SignInRequest {
    String username;
    String password;

    // test
    public SignInRequest() {
    }

    public SignInRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    // TODO : test
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    // TODO : test
    public void setPassword(String password) {
        this.password = password;
    }
}
