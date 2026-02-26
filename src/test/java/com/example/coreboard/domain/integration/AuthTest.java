package com.example.coreboard.domain.integration;

import com.example.coreboard.domain.auth.dto.request.SignInRequest;
import com.example.coreboard.domain.auth.dto.request.SignUpRequest;
import com.example.coreboard.domain.common.config.EmailPhoneNumberManager;
import com.example.coreboard.domain.common.config.PasswordManager;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

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

        @Autowired
        ObjectMapper objectMapper;

        @BeforeEach
        void setUser() {
                long start = System.currentTimeMillis();
                Users user = new Users(
                                "username",
                                passwordEncode.encrypt("password"),
                                emailPhoneNumberEncode.encrypt("email@naver.com"),
                                emailPhoneNumberEncode.encrypt("01012341234"));
                usersRepository.save(user);
                long end = System.currentTimeMillis();
                System.out.println("암호화 시간: " + (end - start) + "ms"); // 443ms
        }

        @Test
        @DisplayName("POST/auth/users")
        void authUsers() throws Exception {
                SignUpRequest request = new SignUpRequest(
                                "username1",
                                "password",
                                "password",
                                "email@naver.com",
                                "01012341234");

                mockMvc.perform(
                                post("/auth/users")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))

                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.username").value("username1"));

                assertThat(usersRepository.count()).isEqualTo(2);
                Users created = usersRepository.findByUsername("username1").get();
                assertThat(created.getUsername()).isEqualTo("username1");
        }

        @Test
        @DisplayName("POST/auth/token")
        void authToken() throws Exception {
                SignInRequest request = new SignInRequest("username", "password");
                mockMvc.perform(
                                post("/auth/token")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))

                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.accessToken").exists());
                assertThat(usersRepository.count()).isEqualTo(1);
                Users token = usersRepository.findByUsername("username").get();
                assertThat(token.getUsername()).isEqualTo("username");
        }
}
