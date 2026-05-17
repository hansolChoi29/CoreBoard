package com.example.coreboard.domain.integration;

import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.common.type.ContentFormat;
import com.example.coreboard.domain.common.util.JwtUtil;
import com.example.coreboard.domain.post.dto.request.CreatePostRequest;
import com.example.coreboard.domain.post.dto.request.UpdatePostRequest;
import com.example.coreboard.domain.post.repository.PostRepository;
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

import java.util.List;

import static com.example.coreboard.domain.support.fixture.BoardFixture.freeBoard;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostIntegrationTest extends IntegrationTestBase {

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private String accessToken;
    private Users user;

    @BeforeEach
    void setup() {
        user = usersRepository.save(
                new Users(
                        "username",
                        "nickname",
                        "password",
                        "email@naver.com",
                        "01012341234",
                        UserRole.USER
                )
        );

        accessToken = JwtUtil.createAccessToken(
                user.getUserId(),
                user.getUsername(),
                user.getRole()
        );
    }

    @Test
    @DisplayName("사용자는_게시글을_작성하고_조회하고_수정하고_삭제할_수_있다")
    void postLifecycle() throws Exception {
        Board board = boardRepository.save(freeBoard());

        CreatePostRequest createRequest = new CreatePostRequest(
                "title",
                "content",
                ContentFormat.MARKDOWN,
                List.of()
        );

        MvcResult createResult = mockMvc.perform(
                        post("/boards/{boardId}/posts", board.getId())
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("게시글이 성공적으로 생성되었습니다."))
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn();

        Long postId = objectMapper
                .readTree(createResult.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();

        assertThat(postRepository.count()).isEqualTo(1);

        mockMvc.perform(
                        get("/posts/{id}", postId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(postId))
                .andExpect(jsonPath("$.data.title").value("title"))
                .andExpect(jsonPath("$.data.content").value("content"));

        mockMvc.perform(
                        get("/boards/{boardId}/posts", board.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글 전체 조회!"))
                .andExpect(jsonPath("$.data.content[0].title").value("title"))
                .andExpect(jsonPath("$.data.content[0].writerName").value("nickname"))
                .andExpect(jsonPath("$.data.pageInfo.page").value(0))
                .andExpect(jsonPath("$.data.pageInfo.size").value(10))
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(1))
                .andExpect(jsonPath("$.data.pageInfo.totalPages").value(1));

        UpdatePostRequest updateRequest = new UpdatePostRequest(
                "newtitle",
                "newcontent",
                ContentFormat.MARKDOWN,
                List.of(),
                List.of()
        );

        mockMvc.perform(
                        put("/posts/{id}", postId)
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(postId));

        mockMvc.perform(
                        get("/posts/{id}", postId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(postId))
                .andExpect(jsonPath("$.data.title").value("newtitle"))
                .andExpect(jsonPath("$.data.content").value("newcontent"));

        mockMvc.perform(
                        delete("/posts/{id}", postId)
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}