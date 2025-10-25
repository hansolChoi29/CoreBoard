package com.example.coreboard.domain.users.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;



class UsersTest {
    @Test
    @DisplayName("기본_생성자_호출_test")
    void noArgsConsrtuctor(){
        Users users = new Users();
        assertNotNull(users);
    }
}