package com.example.coreboard.domain.integration;

import com.example.coreboard.domain.common.config.EmailPhoneNumberManager;
import com.example.coreboard.domain.common.config.PasswordManager;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthTest extends IntegrationTestBase {
    @Autowired
    UsersRepository usersRepository;

    @Autowired
    PasswordManager passwordEncode;

    @Autowired
    EmailPhoneNumberManager emailPhoneNumberEncode;

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void setUser() {
        Users user = new Users(
                "username",
                passwordEncode.encrypt("password"),
                emailPhoneNumberEncode.encrypt("email@naver.com"),
                emailPhoneNumberEncode.encrypt("01012341234"));
        usersRepository.save(user);
    }

    @Test
    @DisplayName("POST/auth/users")
    void authUsers() throws Exception {
        String json = """
                {
                    "username" : "username1",
                    "password" : "password",
                    "confirmPassword" : "password",
                    "email" : "email@naver.com",
                    "phoneNumber" : "01012341234"
                }
                """;
        mockMvc.perform(
                post("/auth/users")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("username1"));
        assertThat(usersRepository.count()).isEqualTo(2);
        Users created = usersRepository.findByUsername("username1").get();
        assertThat(created.getUsername()).isEqualTo("username1");
    }

    @Test
    @DisplayName("POST/auth/token")
    void authToken() throws Exception {
        String json = """
                {
                    "username" : "username",
                    "password" : "password"
                }
                """;

        mockMvc.perform(
                post("/auth/token")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists());
        assertThat(usersRepository.count()).isEqualTo(1);
        Users token = usersRepository.findByUsername("username").get();
        assertThat(token.getUsername()).isEqualTo("username");
    }
}
