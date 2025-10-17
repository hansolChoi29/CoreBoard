package com.example.coreboard.domain.board.controller;

import com.example.coreboard.domain.board.dto.*;
import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.service.BoardService;
import com.example.coreboard.domain.common.exception.GlobalExceptionHandler;
import com.example.coreboard.domain.common.exception.auth.AuthErrorCode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.board.BoardErrorCode;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;
import com.example.coreboard.domain.common.interceptor.AuthInterceptor;
import com.example.coreboard.domain.common.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import static org.mockito.Mockito.*;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class) // JUnit5에 Mockito 기능을 꽂아줌(@Mock/@InjectMocks가 동작하도록
@Import(GlobalExceptionHandler.class) // 글로벌핸들러 등록
class BoardControllerTest {
    // MockMvc : 진짜 톰캣이 없어도 컨트롤러를 테스트할 수 있게 해주는 가짜 브라우저/클라이언트

    private static final String BASE = "/api/board"; // 매핑
    // @MockBean <= 지원종료
    @Mock // 진짜 서비스 대신 가짜(테스트용) 서비스
            BoardService boardService; //DB 안 쓰고 빠르게 테스트

    @InjectMocks // 가짜 서비스(@Mock)를 주입한 컨트롤러 인스턴스
    BoardController boardController;

    MockMvc mockMvc; // 진짜 톰캣/스프링 컨텍스트 없이 컨트롤러만 올려서 웹 호출을 시뮬레이션하는 가짜 클라이언트
    MockMvc mockMvcWithInterceptor;

    // MockMvcBuilders: 가짜 스프링 웹 환경(가짜 HTTP 환경) 만들기
    // standaloneSetup : 테스트할 컨트롤러만 독립적으로 올리겠다
    // setControllerAdvice : 전역 예외처리기 등록
    // setMessageConverters : 객체 자동 변환기 등록
    // MappingJackson2HttpMessageConverter: JSON 변환기

    // 테스트 실행 전에 매번 실행되는 초기 설정 메서드
    @BeforeEach
    // 각 테스트 전에 실행할 준비 단계
    void setup() {
        mockMvc = MockMvcBuilders // MockMvc를 만들어주는 빌더(조립기) 시작
                .standaloneSetup(boardController) // 테스트할 컨트롤러 1개만 독립적으로 올림
                .setControllerAdvice(new GlobalExceptionHandler()) // 전역 예외처리기 등록 (예외 → JSON 응답으로 변환)
                .setMessageConverters(new MappingJackson2HttpMessageConverter()) // JSON <-> 객체 변환기 등록(Jackson)
                .build(); // 위 설정들로 MockMvc 인스턴스 생성

        mockMvcWithInterceptor = MockMvcBuilders // MockMvc를 만들어주는 빌더(조립기) 시작
                .standaloneSetup(boardController) // 테스트할 컨트롤러 1개만 독립적으로 올림
                .setControllerAdvice(new GlobalExceptionHandler()) // 전역 예외처리기 등록 (예외 → JSON 응답으로 변환)
                .setMessageConverters(new MappingJackson2HttpMessageConverter()) // JSON <-> 객체 변환기 등록(Jackson)
                .addInterceptors(new AuthInterceptor()) // 진짜 인터셉터 재현
                .build(); // 위 설정들로 MockMvc 인스턴스 생성
    }

    @Test
    @DisplayName("게시글_생성")
    void create() throws Exception {
        String username = "tester";

        BoardCreateResponse dummy = new BoardCreateResponse(
                1L,
                10L,
                "제목",
                "본문",
                LocalDateTime.now()
        );
        // any는 첫번째 인자(요청 dto), 두번째 인자 tester
        given(boardService.create(any(), eq(username))).willReturn(dummy);

        // HTTP 요청 시뮬
        String json = """
                {
                    "title" : "제목",
                    "content" : "본문"
                }
                """;

        mockMvc.perform(
                        post(BASE) // /api/board로 POST 요청
                                .requestAttr("username", username) // 인터셉터
                                // contentType(JSON) + content(json) : 본문이 JSON이며, 이 문자열이 요청 바디라는 의미
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )

                .andExpect(status().isOk()) // 상태코드 200인지
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // 콘텐츠 타입이 JSON인지
                .andExpect(jsonPath("$.message").value("게시글이 성공적으로 생성되었습니다."))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(10))
                .andExpect(jsonPath("$.data.title").value("제목"))
                .andExpect(jsonPath("$.data.content").value("본문"))
                .andExpect(jsonPath("$.data.createdDate", notNullValue()));
        verify(boardService).create(any(), eq(username)); // 컨트롤러가 진짜로 서비스의 create()를 한 번 호출했는지 확인
    }

    // Mockito 3대 기능
    // 1) eq() : 이 값이 정확히 들어오면 = equals
    // 2) any() : 요청 값이 아무거나 OK 들어와도
    // 3) willThrow() : 이 상황이면 이 예외 던져라

    @Test
    @DisplayName("게시글_생성_유저없음_404")
    void createIsNotUser() throws Exception {
        String json = """
                {
                  "title" : "제목",
                  "content" : "내용"
                }
                """;

        given(boardService.create(any(BoardCreateRequest.class), eq("ghost")))
                .willThrow(new AuthErrorException(AuthErrorCode.NOT_FOUND));

        mockMvc.perform(
                        post(BASE)
                                .requestAttr("username", "ghost")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound()) // 기대 결과
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));

        verify(boardService).create(any(BoardCreateRequest.class), eq("ghost"));
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_생성_권한없음_403")
    void createForbidden() throws Exception {
        String username = "tester";
        String json = """
                {
                    "title" : "제목",
                    "content" : "내용"
                }
                """;
        // Mockito 타입별 매처
        // 1) any() : 아무 객체 허용
        // 2) anyString() : 아무 문자열 허용
        // 3) anyInt() : 아무 int 허용
        // 4) eq(value) : 정확히 이 값일 때만 허용
        given(boardService.create(any(BoardCreateRequest.class), eq(username))).willThrow(new AuthErrorException(AuthErrorCode.FORBIDDEN));
        mockMvc.perform(
                        post(BASE)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));

        verify(boardService).create(any(BoardCreateRequest.class), eq(username));
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_생성_로그인_안함_401")
    void createUnauthorized() throws Exception {
        // setup()에 인터셉터가 있어도/없어도 모두 401 통과되는 이유
        // 컨트롤러에도 401예외처리가 구현되어있다.
        String json = """
                {
                    "title" : "제목",
                    "content" : "내용"
                }
                """;
        mockMvcWithInterceptor.perform(
                        post(BASE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("다시 로그인해 주세요."));

        verifyNoMoreInteractions(boardService);
    }

    // controller test 시 given을 사용하겠다란 말은
    // 서비스를 호출하겠다라는 의미인데, 입력값이 이미 비어있기 때문에 서비스까지 굳이 가지 않겠다 never()
    @Test
    @DisplayName("게시글_생성_제목과_본문_비어있음_400")
    void creatteTitleAndContentIsBlank() throws Exception {
        String json = """
                {
                    "title" : "",
                    "content" :""
                }
                """;
        mockMvc.perform(
                        post(BASE)
                                .requestAttr("username", "tester")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목과 내용은 필수입니다."));
        // 서비스가 단 한번도 호출되면 안 된다.
        verify(boardService, never()).create(any(), anyString());
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_생성_제목_400")
    void createContentIsBlank() throws Exception {
        String json = """
                {
                    "title" : "dsa",
                    "content" : ""
                }
                """;
        mockMvc.perform(
                        post(BASE)
                                .requestAttr("username", "tester")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("내용은 필수입니다."));
        verify(boardService, never()).create(any(), anyString());
    }

    @Test
    @DisplayName("게시글_생성_제목_400")
    void createTitleOrContentIsBlank() throws Exception {
        String json = """
                {
                    "title" : "",
                    "content" : "asda"
                }
                """;
        mockMvc.perform(
                        post(BASE)
                                .requestAttr("username", "tester")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목은 필수입니다."));
        verify(boardService, never()).create(any(), anyString());
    }

    @Test
    @DisplayName("게시글_생성_제목_너무_김_400")
    void createTitleToLong() throws Exception {
        String longTitle = "a".repeat(256);
        String json = String.format("""
                {
                    "title" :"%s",
                    "content" : "zx"
                }
                """, longTitle);
        mockMvc.perform(
                        post(BASE)
                                .requestAttr("username", "tester")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목은 255자 미만이어야 합니다"));
        verify(boardService, never()).create(any(), anyString());
    }

    @Test
    @DisplayName("게시글_생성_본문_너무_김_400")
    void createContentToLong() throws Exception {
        String longContent = "a".repeat(1001);
        String json = String.format("""
                {
                    "title" :"jdsdsd",
                     "content" : "%s"
                }
                """, longContent);
        mockMvc.perform(
                        post(BASE)
                                .requestAttr("username", "tester")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("본문은 1000자 미만이어야 합니다"));
        verify(boardService, never()).create(any(), anyString());
    }

    @Test
    @DisplayName("게시글_생성_이미_존재하는_제목_409")
    void createTitleDuplicated() throws Exception {
        String json = """
                {
                    "title":"중복제목",
                    "content":"내용"
                }
                """;
        String username = "tester";

        given(boardService.create(any(), eq(username))).willThrow(new BoardErrorException(BoardErrorCode.TITLE_DUPLICATED));

        mockMvc.perform(
                        post(BASE)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isConflict()) // 중복데이터가 있을 필요가 없음 mock 중복이야!라고 던진다고 가정
                .andExpect(jsonPath("$.message").value("이미 사용 중인 제목입니다."));
        verify(boardService).create(any(), eq(username));
        verifyNoMoreInteractions(boardService);
    }

    @Test
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
                        get(BASE + "/{id}", id)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("게시글 단건 조회!"))
                .andExpect(jsonPath("$.data.id").value(id));
        // verify 왜 필요한가?
        // 응답만 보는 게 아니라 컨트롤러가 서비스 레이어를 올바르게 호출했는지도 확인해야 함
        // HTTP 요청 -> Controller -> Service 호출의 연결이 잘 이루어졌는지 확인
        verify(boardService).findOne(eq(id));
        // verifyNoMoreInteractions : 방금 findOne말고는 서비스에 다른 호출은 없어야 한다
        // 왜 다른 호출은 없어야 하는가? -> 테스트의 목적은 딱 필요한 동작만 했는가
        // 언제 쓰는 게 적절할까? -> 해당 엔드포인트가 서비스의 한 메서드만 호출해야 할 때
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_단건_조회_존재하지_않는_게시글_404")
    void getOneIsNotFoundBoard() throws Exception {
        long id = 10;

        given(boardService.findOne(eq(id))).willThrow(new BoardErrorException(BoardErrorCode.POST_NOT_FOUND));

        mockMvc.perform(
                        get(BASE + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 게시글입니다."))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(boardService).findOne(eq(id));
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글 전체 조회")
    void getAll() throws Exception {
        BoardSummaryResponse item = new BoardSummaryResponse(
                1L, 10L, "제목", LocalDateTime.now()
        );

        // content는 목록이라 리스트인데, 테스트 시 목록 1건 흉내냄
        PageResponse<BoardSummaryResponse> pageResponse = new PageResponse<>(List.of(item), 0, 10, 1L);
        ApiResponse<PageResponse<BoardSummaryResponse>> body = ApiResponse.ok(pageResponse, "게시글 전체 조회!");

        given(boardService.findAll(eq(0), eq(10), eq("asc"))).willReturn(body);
        mockMvc.perform(
                        get(BASE)
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "asc")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                // andExpect : HTTP 응답 내용을 검증 (상태코드, JSON 본문, 메시지, 필드 값 등)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글 전체 조회!"))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].userId").value(10))
                .andExpect(jsonPath("$.data.content[0].title").value("제목"))
                .andExpect(jsonPath("$.data.content[0].createdDate", notNullValue()))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10));
        // verify : 컨트롤러가 mock서비스에게 어떤 호출을 했는지를 검증
        verify(boardService).findAll(eq(0), eq(10), eq("asc"));
    }

    // 성공 테스트는 서비스가 필요한데 왜 예외 시 불필요한가?
    // 테스트 시 진짜 서비스(DB)를 쓰지 않음
    // 그래서 가짜 서비스(mock), 이런 데이터를 돌려준다고 약속(stub)해야 함 <= given
    @Test
    @DisplayName("게시글_전체_조회_Page_0_이상_400")
    void getAllPageNegatice() throws Exception {
        mockMvc.perform(
                        get(BASE)
                                .param("page", "-1")
                                .param("size", "10")
                                .accept(MediaType.APPLICATION_JSON)
                )

                // andExpect로 data를 다 검사해야 할까?
                // 예외 시 보통 message와 상태코드가 핵심
                // data는 대부분 null이거나 비어있음
                // 그래서 메시지/상태코드 위주로 검증하면 충분

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("page는 0이상이어야 합니다."));
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_전체_조회_Size_10_이상_400")
    void getAllSizeTooLonger() throws Exception {
        mockMvc.perform(
                        get(BASE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("page", "0")
                                .param("size", "11")
                                .param("sort", "asc")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("zise는 최대 10이하이어야 합니다"));
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_전체_조회_정렬_방향_잘못됨_40")
    void getAllInvalidSortDirection() throws Exception {
        mockMvc.perform(
                        get(BASE)
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "wrong")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("정렬 방향은 asc 또는 desc만 허용됩니다."));
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글 수정")
    void update() throws Exception {
        Long boardId = 1L;
        long userId = 10L;
        BoardUpdateResponse dummy = new BoardUpdateResponse(
                boardId,
                userId,
                "제목",
                "내용",
                LocalDateTime.now()
        );
        given(boardService.update(any(), eq("tester"), eq(boardId))).willReturn(dummy);

        String json = """
                {
                    "title" : "제목",
                    "content" : "내용"
                }
                """;

        mockMvc.perform(
                        put(BASE + "/{id}", boardId)
                                .requestAttr("username", "tester")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)

                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("게시글 수정 완료!"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.title").value("제목"))
                .andExpect(jsonPath("$.data.content").value("내용"))
                .andExpect(jsonPath("$.data.lastModifiedDate", notNullValue()));
        verify(boardService).update(any(), eq("tester"), eq(boardId));
    }

    @Test
    @DisplayName("게시글_수정_비로그인_401")
    void updateIsUnauthorized() throws Exception {
        long id = 1;

        String json = """
                {
                    "title" : "제목",
                    "content" : "본문"
                }
                """;

        mockMvcWithInterceptor.perform(
                        put(BASE + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("다시 로그인해 주세요."));
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_수정_존재하지_않는_게시글_404")
    void updateNotFound() throws Exception {
        long id = 1;
        String username = "tester";
        String json = """
                {
                    "title" : "제목",
                    "content" : "본문"
                }
                """;

        given(boardService.update(any(BoardUpdateRequest.class), eq(username), eq(id))).willThrow(new BoardErrorException(BoardErrorCode.POST_NOT_FOUND));

        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 게시글입니다."));
        verify(boardService).update(any(BoardUpdateRequest.class), eq(username), eq(id));
    }

    @Test
    @DisplayName("게시글_수정_403")
    void updateForbidden() throws Exception {
        String username = "tester";
        long id = 1;
        String json = """
                {
                    "title" : "제목",
                    "content" : "본문"
                }
                """;
        given(boardService.update(any(BoardUpdateRequest.class), eq(username), eq(id))).willThrow(new AuthErrorException(AuthErrorCode.FORBIDDEN));

        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
        verify(boardService).update(any(BoardUpdateRequest.class), eq(username), eq(id));
    }
    // $) 게시글 수정하려는데 타이틀 길이 초과 (400)
    // 5) 게시글 수정하려는데 본문 길이 초과 (400)
    // 6) 게시글 수정하려는데 타이틀 또는 본문 같이 빈 값임 (400)
    // 7) 게시글 수정하려는데 타이틀만 빈값 (400)
    // 8) 게시글 수정하려는데 본문만 빈값 (400)
    // 9) 게시글 수정하려는데 이미 사용 중인 타이틀임 (409)
    // 10) 게시글 삭제하려는데 삭제된 게시글임 (404)

    // 존재하지 않은 게시글
    @Test
    @DisplayName("게시글 삭제")
    void deleted() throws Exception {
        long boardId = 1;
        long userId = 1;

        // response에서 생성자 매개변수에 엔티티를 받고 있음
        Board board = mock(Board.class);

        // 삭제할 데이터 넣기
        when(board.getId()).thenReturn(boardId);
        when(board.getUserId()).thenReturn(userId);
        when(board.getTitle()).thenReturn("제목");

        BoardDeleteResponse dummy = new BoardDeleteResponse(board);

        given(boardService.delete(eq("tester"), eq(userId))).willReturn(dummy);

        mockMvc.perform(
                        delete(BASE + "/{id}", boardId)
                                .requestAttr("username", "tester")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("게시글 삭제완료!"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.title").value("제목"));
        verify(boardService).delete(eq("tester"), eq(userId));
    }
}