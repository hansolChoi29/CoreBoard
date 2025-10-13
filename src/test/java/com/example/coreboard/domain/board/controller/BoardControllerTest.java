package com.example.coreboard.domain.board.controller;

import com.example.coreboard.domain.board.dto.BoardCreateResponse;
import com.example.coreboard.domain.board.dto.BoardGetOneResponse;
import com.example.coreboard.domain.board.service.BoardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
class BoardControllerTest {
    // MockMvc : 진짜 톰캣이 없어도 컨트롤러를 테스트할 수 있게 해주는 가짜 브라우저/클라이언트

    private static final String BASE = "/api/board"; // 매핑
    // @MockBean <= 지원종료
    @Mock
    BoardService boardService;

    @InjectMocks
    BoardController boardController;

    MockMvc mockMvc;


    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(boardController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter()) // JSON 바인딩
                .build();
    }

    @Test
    @DisplayName("게시글 생성 성공")
    void createBoard() throws Exception {


        BoardCreateResponse dummy = new BoardCreateResponse(1L, 10L, "제목", "본문", LocalDateTime.now());

        given(boardService.create(any(), eq("tester"))).willReturn(dummy);

        String json = """
                {
                    "boardTitle" : "제목",
                    "boardContents" : "본문"
                }
                """;

        mockMvc.perform(
                        post(BASE)
                                .requestAttr("username", "tester")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                ).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("게시글이 성공적으로 생성되었습니다."))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(10))
                .andExpect(jsonPath("$.data.boardTitle").value("제목"))
                .andExpect(jsonPath("$.data.boardContents").value("본문"))
                .andExpect(jsonPath("$.data.createdDate", notNullValue()));
        verify(boardService).create(any(), eq("tester"));
    }

    @Test
    @DisplayName("게시글 단건 조회 성공")
    void getOne() throws Exception {

    }

    @Test
    void getAll() {
    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }
}