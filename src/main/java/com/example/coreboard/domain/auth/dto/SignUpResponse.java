package com.example.coreboard.domain.auth.dto;

public class SignUpResponse {
    private String username;
    private String message;

    public SignUpResponse(String username,
                          String message) {
        this.username = username;
        this.message = message;
    }


    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }
}
