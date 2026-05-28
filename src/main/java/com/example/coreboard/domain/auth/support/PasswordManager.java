package com.example.coreboard.domain.auth.support;


import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class PasswordManager {
    private static final int COST = 12;

    public String encrypt(String password) {
        return BCrypt.withDefaults().hashToString(COST, password.toCharArray());
    }

    public boolean matches(
            String inputPassword,
            String storedHash
    ) {
        if (storedHash == null || storedHash.isEmpty())
            return false;

        return BCrypt.verifyer()
                .verify(inputPassword.toCharArray(), storedHash)
                .verified;
    }
}
