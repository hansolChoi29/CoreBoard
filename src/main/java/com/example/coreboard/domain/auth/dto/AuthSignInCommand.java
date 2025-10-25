package com.example.coreboard.domain.auth.dto;

public class AuthSignInCommand {
    String username;
    String password;


    public AuthSignInCommand(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        // TODO : test
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        // TODO : test
        this.password = password;
    }
}
