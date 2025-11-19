package com.example.coreboard.domain.auth.dto.response;


public class SignUpResponse {
    private final String username;

    public SignUpResponse(
            String username

    ) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
