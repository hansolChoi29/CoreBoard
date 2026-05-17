package com.example.coreboard.domain.integration;

import com.example.coreboard.domain.auth.dto.request.SignInRequest;
import com.example.coreboard.domain.auth.dto.request.SignUpRequest;
import com.example.coreboard.domain.users.entity.UserRole;
import com.example.coreboard.domain.users.repository.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthIntegrationTest extends IntegrationTestBase {

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("사용자는_회원가입_후_로그인할_수_있다")
    void signUpAndSignIn_success() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest(
                "username1",
                "nickname",
                "password",
                "password",
                "email@naver.com",
                "01012341234",
                UserRole.USER
        );

        mockMvc.perform(
                        post("/auth/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("username1"));

        assertThat(usersRepository.findByUsername("username1")).isPresent();

        SignInRequest signInRequest = new SignInRequest("username1", "password");

        mockMvc.perform(
                        post("/auth/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists());
    }
}