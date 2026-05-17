package com.example.coreboard.domain.integration;

import com.example.coreboard.domain.board.dto.request.CreateBoardRequest;
import com.example.coreboard.domain.board.dto.request.UpdateBoardRequest;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.common.util.JwtUtil;
import com.example.coreboard.domain.users.entity.UserRole;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BoardIntegrationTest extends IntegrationTestBase {

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private String adminAccessToken;

    @BeforeEach
    void setup() {
        Users admin = usersRepository.save(
                new Users(
                        "admin",
                        "adminNickname",
                        "password",
                        "admin@test.com",
                        "01012341234",
                        UserRole.ADMIN
                )
        );

        adminAccessToken = JwtUtil.createAccessToken(
                admin.getUserId(),
                admin.getUsername(),
                admin.getRole()
        );
    }

    @Test
    @DisplayName("관리자는_게시판을_생성하고_조회하고_수정하고_삭제할_수_있다")
    void boardLifecycle() throws Exception {
        CreateBoardRequest createRequest = new CreateBoardRequest(
                "자유게시판",
                "free",
                true,
                true,
                false,
                0,
                UserRole.USER
        );

        MvcResult createResult = mockMvc.perform(
                        post("/admin/boards")
                                .header("Authorization", "Bearer " + adminAccessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("성공적으로 게시판이 생성되었습니다."))
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn();

        Long boardId = objectMapper
                .readTree(createResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();

        assertThat(boardRepository.findById(boardId)).isPresent();

        mockMvc.perform(
                        get("/boards/{id}", boardId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("성공적으로 불러왔습니다."))
                .andExpect(jsonPath("$.data.id").value(boardId))
                .andExpect(jsonPath("$.data.name").value("자유게시판"))
                .andExpect(jsonPath("$.data.slug").value("free"));

        mockMvc.perform(
                        get("/boards")
                                .param("page", "0")
                                .param("size", "10")
                                .param("direction", "DESC")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].boardId").value(boardId))
                .andExpect(jsonPath("$.data.content[0].name").value("자유게시판"))
                .andExpect(jsonPath("$.data.content[0].slug").value("free"))
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(1));

        UpdateBoardRequest updateRequest = new UpdateBoardRequest(
                boardId,
                "질문게시판",
                "qna",
                true,
                true,
                false,
                0
        );

        mockMvc.perform(
                        patch("/admin/boards/{id}", boardId)
                                .header("Authorization", "Bearer " + adminAccessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("성공적으로 수정되었습니다."))
                .andExpect(jsonPath("$.data.id").value(boardId));

        mockMvc.perform(
                        get("/boards/{id}", boardId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(boardId))
                .andExpect(jsonPath("$.data.name").value("질문게시판"))
                .andExpect(jsonPath("$.data.slug").value("qna"));

        mockMvc.perform(
                        delete("/admin/boards/{id}", boardId)
                                .header("Authorization", "Bearer " + adminAccessToken)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}