package com.example.coreboard.domain.integration;

import com.example.coreboard.domain.auth.dto.request.SignUpRequest;
import com.example.coreboard.domain.common.config.EmailPhoneNumberManager;
import com.example.coreboard.domain.common.config.PasswordManager;
import com.example.coreboard.domain.common.util.JwtUtil;
import com.example.coreboard.domain.users.entity.UserRole;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminIntegrationTest extends IntegrationTestBase {
    @Autowired
    UsersRepository usersRepository;

    @Autowired
    PasswordManager passwordManager;

    @Autowired
    EmailPhoneNumberManager emailPhoneNumberManager;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("관리자는_초기설정_후_사용자목록을_조회하고_사용자를_관리자로_승급할_수_있다")
    void adminLifecycle() throws Exception {
        SignUpRequest setupRequest = new SignUpRequest(
                "admin",
                "adminNickname",
                "password",
                "password",
                "admin@test.com",
                "01012341234",
                UserRole.ADMIN
        );

        mockMvc.perform(
                        post("/admin/setup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(setupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원가입 성공"))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));

        Users admin = usersRepository.findByUsername("admin").orElseThrow();
        assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);

        String adminAccessToken = JwtUtil.createAccessToken(
                admin.getUserId(),
                admin.getUsername(),
                admin.getRole()
        );

        Users targetUser = usersRepository.save(
                new Users(
                        "user",
                        "userNickname",
                        passwordManager.encrypt("password"),
                        emailPhoneNumberManager.encrypt("user@test.com"),
                        emailPhoneNumberManager.encrypt("01099998888"),
                        UserRole.USER
                )
        );

        mockMvc.perform(
                        get("/admin/users")
                                .header("Authorization", "Bearer " + adminAccessToken)
                                .requestAttr("username", "admin")
                                .param("role", "ADMIN")
                                .param("page", "0")
                                .param("size", "20")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("성공적으로 관리자 목록을 불러왔습니다."))
                .andExpect(jsonPath("$.data.content[0].username").value("admin"))
                .andExpect(jsonPath("$.data.content[0].role").value("ADMIN"))
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(1));

        mockMvc.perform(
                        patch("/admin/users/{id}/role", targetUser.getUserId())
                                .header("Authorization", "Bearer " + adminAccessToken)
                                .param("role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용자 권한이 변경되었습니다."))
                .andExpect(jsonPath("$.data.id").value(targetUser.getUserId()))
                .andExpect(jsonPath("$.data.username").value("user"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));

        mockMvc.perform(
                        get("/admin/users")
                                .header("Authorization", "Bearer " + adminAccessToken)
                                .requestAttr("username", "admin")
                                .param("role", "ADMIN")
                                .param("page", "0")
                                .param("size", "20")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(2));
    }
}