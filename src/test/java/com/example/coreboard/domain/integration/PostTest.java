package com.example.coreboard.domain.integration;

import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.post.dto.request.PostCreateRequest;
import com.example.coreboard.domain.post.dto.request.PostUpdateRequest;
import com.example.coreboard.domain.post.entity.ContentFormat;
import com.example.coreboard.domain.post.entity.Post;
import com.example.coreboard.domain.post.repository.PostRepository;
import com.example.coreboard.domain.users.entity.UserRole;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static com.example.coreboard.domain.support.fixture.BoardFixture.*;
import org.springframework.http.MediaType;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BoardTest extends IntegrationTestBase {
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
        Users user = usersRepository.save(new Users("username", "nickname", "password", "email@naver.com", "01012341234", UserRole.USER));

        String secret = "test-jwt-secret-key-for-coreboard";
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        this.savedUserId = user.getUserId();
        this.savedUsername = user.getUsername();

        this.accessToken = Jwts.builder()
                .setSubject("username")
                .claim("userId", user.getUserId())
                .claim("type", "access")
                .setExpiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    @DisplayName("POST/board")
    void boardCreate() throws Exception {
        Board board = freeBoard();
        PostCreateRequest request = new PostCreateRequest(board.getId(), "title", "content", ContentFormat.MARKDOWN);

        mockMvc.perform(
                        post("/board")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("title"))
                .andExpect(jsonPath("$.data.content").value("content"));
        assertThat(postRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("GET/board/id")
    void getOn() throws Exception {
        Board board = freeBoard();
        Users user = usersRepository.save(new Users("username2", "nickname", "password", "qwe@qwe.com", "010-1234-1234", UserRole.USER));
        Post saved = postRepository.save(new Post(board, user, "title", "content", ContentFormat.MARKDOWN));
        Long realId = saved.getId();

        mockMvc.perform(
                        get("/board/{id}", realId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(realId))
                .andExpect(jsonPath("$.data.title").value("title"))
                .andExpect(jsonPath("$.data.content").value("content"));
    }

    @Test
    @DisplayName("GET/board")
    void getAll() throws Exception {
        Board board = freeBoard();
        Users user = usersRepository.save(new Users("username2", "nickname", "password", "qwe@qwe.com", "010-1234-1234", UserRole.USER));
        postRepository.save(new Post(board, user, "title", "content", ContentFormat.MARKDOWN));

        mockMvc.perform(
                        get("/board")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("size", "10")
                                .param("sort", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasNext").value("false"));
    }

    @Test
    @DisplayName("PUT/board/id")
    void put() throws Exception {
        Board board = freeBoard();
        Users user = usersRepository.findByUsername("username").orElseThrow();
        Post saved = postRepository.save(new Post(board, user, "title", "content", ContentFormat.MARKDOWN));

        Long realId = saved.getId();
        PostUpdateRequest reqeust = new PostUpdateRequest("newtitle", "newcontent", ContentFormat.MARKDOWN);

        mockMvc.perform(
                        MockMvcRequestBuilders.put("/board/{id}", realId)
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqeust)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(realId));
    }

    @Test
    @DisplayName("DELETE/board/id")
    void delete() throws Exception {
        Board board = freeBoard();

        Users user = usersRepository.findByUsername("username").orElseThrow();
        Post saved = postRepository.save(new Post(board, user, "title", "content", ContentFormat.MARKDOWN));

        Long realId = saved.getId();

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/board/{id}", realId)
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
