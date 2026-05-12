package com.example.coreboard.domain.integration;

import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.common.util.JwtUtil;
import com.example.coreboard.domain.post.dto.request.CreatePostRequest;
import com.example.coreboard.domain.post.dto.request.UpdatePostRequest;
import com.example.coreboard.domain.common.type.ContentFormat;
import com.example.coreboard.domain.post.entity.Post;
import com.example.coreboard.domain.post.repository.PostRepository;
import com.example.coreboard.domain.users.entity.UserRole;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.example.coreboard.domain.support.fixture.BoardFixture.*;

import org.springframework.http.MediaType;


import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostTest extends IntegrationTestBase {
    Long boardId = 1L;
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
    private Long savedUserId;
    private String savedUsername;

    @BeforeEach
    void setup() {
        Users user = usersRepository.save(
                new Users(
                        "username",
                        "nickname",
                        "password",
                        "email@naver.com",
                        "01012341234",
                        UserRole.USER
                )
        );
        this.savedUserId = user.getUserId();
        this.savedUsername = user.getUsername();
        this.accessToken = JwtUtil.createAccessToken(
                user.getUserId(),
                user.getUsername(),
                user.getRole()
        );
    }

    @Test
    @DisplayName("POST/posts")
    void boardCreate() throws Exception {
        Board board = boardRepository.save(freeBoard());
        CreatePostRequest request = new CreatePostRequest("title", "content", ContentFormat.MARKDOWN, List.of());
        mockMvc.perform(
                        post("/boards/{boardId}/posts", board.getId())
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("게시글이 성공적으로 생성되었습니다."))
                .andExpect(jsonPath("$.data.id").exists());
        assertThat(postRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("GET/posts/id")
    void getOne() throws Exception {
        Board board = boardRepository.save(freeBoard());
        Users user = usersRepository.save(new Users("username2", "nickname", "password", "qwe@qwe.com", "010-1234-1234", UserRole.USER));
        Post saved = postRepository.save(new Post(board, user, "title", "content", ContentFormat.MARKDOWN));
        Long realId = saved.getId();
        mockMvc.perform(
                        get("/posts/{id}", realId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(realId))
                .andExpect(jsonPath("$.data.title").value("title"))
                .andExpect(jsonPath("$.data.content").value("content"));
    }

    @Test
    @DisplayName("GET /boards/{boardId}/posts")
    void getAll() throws Exception {
        Board board = boardRepository.save(freeBoard());
        Users user = usersRepository.save(new Users(
                "username2",
                "nickname",
                "password",
                "qwe@qwe.com",
                "010-1234-1234",
                UserRole.USER
        ));
        postRepository.save(new Post(
                board,
                user,
                "title",
                "content",
                ContentFormat.MARKDOWN
        ));
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
    }

    @Test
    @DisplayName("PUT/posts/id")
    void put() throws Exception {
        Board board = boardRepository.save(freeBoard());
        Users user = usersRepository.findByUsername("username").orElseThrow();
        Post saved = postRepository.save(new Post(board, user, "title", "content", ContentFormat.MARKDOWN));
        Long realId = saved.getId();
        UpdatePostRequest reqeust = new UpdatePostRequest(
                "newtitle",
                "newcontent",
                ContentFormat.MARKDOWN,
                List.of(),
                List.of()
        );
        mockMvc.perform(
                        MockMvcRequestBuilders.put("/posts/{id}", realId)
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqeust)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(realId));
    }

    @Test
    @DisplayName("DELETE/posts/id")
    void delete() throws Exception {
        Board board = boardRepository.save(freeBoard());

        Users user = usersRepository.findByUsername("username").orElseThrow();
        Post saved = postRepository.save(new Post(board, user, "title", "content", ContentFormat.MARKDOWN));

        Long realId = saved.getId();
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/posts/{id}", realId)
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
