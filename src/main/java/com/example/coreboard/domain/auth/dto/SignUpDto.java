package com.example.coreboard.domain.auth.dto;

import com.example.coreboard.domain.users.entity.UserRole;

public class SignUpDto {
    String username;
    UserRole role;

    public SignUpDto(
            String username,
            UserRole role
    ) {
        this.username = username;
        this.role = role;
    }

    public UserRole getRole() {
        return role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}