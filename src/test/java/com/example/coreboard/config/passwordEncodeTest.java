package com.example.coreboard.config;

import com.example.coreboard.domain.common.config.PasswordEncode;

public class passwordEncodeTest {
    public static void main(String[] args) {
        PasswordEncode passwordEncode = new PasswordEncode();
        String hash = passwordEncode.encrypt("1234abcd");
        System.out.println(hash);
    }
}
