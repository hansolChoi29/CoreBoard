package com.example.coreboard.config;

import com.example.coreboard.domain.common.config.PasswordEncode;
import org.junit.jupiter.api.Test;

public class passwordEncodeTest {
    @Test
    void passwordEncodeTest() {
        PasswordEncode passwordEncode = new PasswordEncode();
        String hash = passwordEncode.encrypt("1234abcd");
        System.out.println(hash);
    }
}
