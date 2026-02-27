package com.example.coreboard.domain.auth.dto;

public class SignUpDto {
    String username;

    public SignUpDto(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}