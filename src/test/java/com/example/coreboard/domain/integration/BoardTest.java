package com.example.coreboard.domain.integration;

import com.example.coreboard.domain.board.dto.request.BoardCreateRequest;
import com.example.coreboard.domain.board.dto.request.BoardUpdateRequest;
import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.http.MediaType;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BoardTest extends IntegrationTestBase {
        @Autowired
        BoardRepository boardRepository;

        @Autowired
        UsersRepository usersRepository;

        @Autowired
        MockMvc mockMvc;

        @Autowired
        ObjectMapper objectMapper;

        private String accessToken;
        private Long savedUserId;
        private String savedUsername;

        @BeforeEach
        void setup() {
                Users user = usersRepository.save(new Users("username", "password", "email@naver.com", "01012341234"));

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
                BoardCreateRequest request = new BoardCreateRequest("title", "content");

                mockMvc.perform(
                                post("/board")
                                                .header("Authorization", "Bearer " + accessToken)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.title").value("title"))
                                .andExpect(jsonPath("$.data.content").value("content"));
                assertThat(boardRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("GET/board/id")
        void getOn() throws Exception {
                Board board = new Board(
                                null,
                                10L,
                                "title",
                                "content",
                                LocalDateTime.now(),
                                LocalDateTime.now());
                Board saved = boardRepository.save(board);
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
                Board board = new Board(
                                null,
                                10L,
                                "title",
                                "content",
                                LocalDateTime.now(),
                                LocalDateTime.now());
                boardRepository.save(board);
                mockMvc.perform(
                                get("/board")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .param("page", "0")
                                                .param("size", "10")
                                                .param("sort", "asc"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.page").value("0"))
                                .andExpect(jsonPath("$.data.size").value("10"));
        }

        @Test
        @DisplayName("PUT/board/id")
        void put() throws Exception {
                Board board = new Board(
                                null,
                                savedUserId,
                                "title",
                                "content",
                                LocalDateTime.now(),
                                LocalDateTime.now());

                Board saved = boardRepository.save(board);
                Long realId = saved.getId();
                BoardUpdateRequest reqeust = new BoardUpdateRequest("newtitle", "newcontent");

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
                Board board = new Board(
                                null,
                                savedUserId,
                                "title",
                                "content",
                                LocalDateTime.now(),
                                LocalDateTime.now());
                Board saved = boardRepository.save(board);
                Long realId = saved.getId();

                mockMvc.perform(
                                MockMvcRequestBuilders.delete("/board/{id}", realId)
                                                .header("Authorization", "Bearer " + accessToken)
                                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").isEmpty());
        }

        @TestConfiguration
        static class NoAuthForIntegrationTest implements WebMvcConfigurer {
                @Override
                public void addInterceptors(InterceptorRegistry registry) {

                }
        }
}
