package com.example.coreboard.domain.auth.dto.command;

public record SignInCommand(
        String username,
        String password
) {
}
