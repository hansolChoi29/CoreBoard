package com.example.coreboard.domain.board.controller;

import com.example.coreboard.domain.board.dto.BoardCreateResponse;
import com.example.coreboard.domain.board.dto.BoardGetOneResponse;
import com.example.coreboard.domain.board.dto.BoardUpdateResponse;
import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.service.BoardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class) // JUnit5에 Mockito 기능을 꽂아줌(@Mock/@InjectMocks가 동작하도록
class BoardControllerTest {
    // MockMvc : 진짜 톰캣이 없어도 컨트롤러를 테스트할 수 있게 해주는 가짜 브라우저/클라이언트

    private static final String BASE = "/api/board"; // 매핑
    // @MockBean <= 지원종료
    @Mock // 진짜 서비스 대신 가짜(테스트용) 서비스
            BoardService boardService; //DB 안 쓰고 빠르게 테스트

    @InjectMocks // 가짜 서비스(@Mock)를 주입한 컨트롤러 인스턴스
    BoardController boardController;

    MockMvc mockMvc; // 진짜 톰캣/스프링 컨텍스트 없이 컨트롤러만 올려서 웹 호출을 시뮬레이션하는 가짜 클라이언트


    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(boardController)
                // setMessageConverters : JSON ↔ 객체 변환기(Jackson)를 수동 등록
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    @DisplayName("게시글 생성 성공")
    void createBoard() throws Exception {

        // 컨트롤러가 서비스의 create(dto, "tester"부르면 무조건 dummy 돌려주라고 규칙 심음
        BoardCreateResponse dummy = new BoardCreateResponse(
                1L,
                10L,
                "제목",
                "본문",
                LocalDateTime.now()
        );
        // any는 첫번째 인자(요청 dto), 두번째 인자 tester
        given(boardService.create(any(), eq("tester"))).willReturn(dummy);

        // HTTP 요청 시뮬
        String json = """
                {
                    "boardTitle" : "제목",
                    "boardContents" : "본문"
                }
                """;

        mockMvc.perform(
                        post(BASE) // /api/board로 POST 요청
                                .requestAttr("username", "tester") // 인터셉터
                                // contentType(JSON) + content(json) : 본문이 JSON이며, 이 문자열이 요청 바디라는 의미
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )

                .andExpect(status().isOk()) // 상태코드 200인지
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // 콘텐츠 타입이 JSON인지
                .andExpect(jsonPath("$.message").value("게시글이 성공적으로 생성되었습니다."))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(10))
                .andExpect(jsonPath("$.data.boardTitle").value("제목"))
                .andExpect(jsonPath("$.data.boardContents").value("본문"))
                .andExpect(jsonPath("$.data.createdDate", notNullValue()));
        verify(boardService).create(any(), eq("tester")); // 컨트롤러가 진짜로 서비스의 create()를 한 번 호출했는지 확인
    }

    @Test
    @Timeout(5)
    @DisplayName("게시글 단건 조회 성공")
    void getOne() throws Exception {
        long id = 1;
        BoardGetOneResponse dummy = new BoardGetOneResponse(
                id,
                10L,
                "제목",
                "본문",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        // 요청이 1개라서 eq(id) 하나만
        given(boardService.findOne(eq(id))).willReturn(dummy);

        mockMvc.perform(
                        get(BASE + "/{id}", 1L)
                                .accept(MediaType.APPLICATION_JSON)

                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("게시글 단건 조회!"))
                .andExpect(jsonPath("$.data.id").value(1));
        verify(boardService).findOne(eq(id));
    }

    @Test
    void getAll() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("boardTitle").ascending());
        Board dummy = new Board(
                1L,
                "제목",
                "내용",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Page<Board> page = new PageImpl<>(List.of(dummy), pageable, 1);

        given(boardService.findAll(any(Pageable.class))).willReturn(page);

        mockMvc.perform(
                        get(BASE)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("게시글 전체 조회!"))
                .andExpect(jsonPath("$.data.content[0].boardTitle").value("제목"))
                .andExpect(jsonPath("$.data.content[0].boardContents").value("내용"))
                .andExpect(jsonPath("$.data.content[0].createdDate", notNullValue()))
                .andExpect(jsonPath("$.data.content[0].lastModifiedDate", notNullValue()));
        verify(boardService).findAll(any(Pageable.class));


    }

    @Test
    void update() throws Exception {
        long userId = 10L;
        BoardUpdateResponse dummy = new BoardUpdateResponse(
                1L,
                userId,
                "제목",
                "내용",
                LocalDateTime.now()
        );
        given(boardService.update(any(), eq("tester"), eq(userId))).willReturn(dummy);

        String json = """
                {
                    "boardTitle":"제목",
                    "boardContents":"내용"
                }
                """;
        mockMvc.perform(
                        post(BASE + "/{id}", userId)
                                .requestAttr("username", "tester")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)

                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("게시글 수정 완료!"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.boardTitle").value("제목"))
                .andExpect(jsonPath("$.data.boardContents").value("내용"))
                .andExpect(jsonPath("$.data.lastModifiedDate", notNullValue()));
        verify(boardService).update(any(), eq("tester"), eq(userId));
    }

    @Test
    void delete() {
    }
}