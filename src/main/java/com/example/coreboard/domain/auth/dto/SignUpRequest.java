package com.example.coreboard.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;


public class SignUpRequest {
    @NotBlank
    private String username;
    private String password;
    private String email;
    private String phoneNumber;

    public SignUpRequest(String username, String password, String email, String phoneNumber) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}

