package com.example.coreboard.domain.auth.dto.command;

import jakarta.validation.constraints.NotBlank;

public class SignUpCommand {
    @NotBlank
    private String username;
    private String nickname;
    private String password;
    private String confirmPassword;
    private String email;
    private String phoneNumber;

    public SignUpCommand(
            String username,
            String nickname,
            String password,
            String confirmPassword,
            String email, String phoneNumber
    ) {
        this.username = username;
        this.nickname = nickname;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getNickname() {
        return nickname;
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
