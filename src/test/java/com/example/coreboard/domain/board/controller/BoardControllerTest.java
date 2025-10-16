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
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
//                .addInterceptors(new AuthInterceptor()) // 진짜 인터셉터 재현
                .build(); // 위 설정들로 MockMvc 인스턴스 생성
    }

    @Test
    @DisplayName("게시글_생성")
    void create() throws Exception {

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
                    "title" : "제목",
                    "content" : "본문"
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
                .andExpect(jsonPath("$.data.title").value("제목"))
                .andExpect(jsonPath("$.data.content").value("본문"))
                .andExpect(jsonPath("$.data.createdDate", notNullValue()));
        verify(boardService).create(any(), eq("tester")); // 컨트롤러가 진짜로 서비스의 create()를 한 번 호출했는지 확인
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
                        post("/api/board")
                                .requestAttr("username", "ghost")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound()) // 기대 결과
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));

        verify(boardService).create(any(BoardCreateRequest.class), eq("ghost"));
    }

    @Test
    @DisplayName("게시글_생성_권한없음_403")
    void createForbidden() throws Exception {
        String json = """
                {
                    "title" : "제목",
                    "content" : "내용"
                }
                """;
        String username = "tester";
        // Mockito 타입별 매처
        // 1) any() : 아무 객체 허용
        // 2) anyString() : 아무 문자열 허용
        // 3) anyInt() : 아무 int 허용
        // 4) eq(value) : 정확히 이 값일 때만 허용
        given(boardService.create(any(BoardCreateRequest.class), eq(username))).willThrow(new AuthErrorException(AuthErrorCode.FORBIDDEN));
        mockMvc.perform(
                        post("/api/board")
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));

        verify(boardService).create(any(BoardCreateRequest.class), eq(username));
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
        mockMvc.perform(
                        post("/api/board")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("다시 로그인해 주세요."));

        verify(boardService, never()).create(any(BoardCreateRequest.class), anyString());
    }

    // controller test 시 given을 사용하겠다란 말은
    // 서비스를 호출하겠다라는 의미인데, 서비스 테스트에서 진행할 예정이라 never로 한다
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
                        post("/api/board")
                                .requestAttr("username", "tester")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목과 내용은 필수입니다."));
        // 서비스가 단 한번도 호출되면 안 된다.
        verify(boardService, never()).create(any(), anyString());
    }

    @Test
    @DisplayName("게시글_생성_제목_400")
    void createContentIsBlank() throws Exception{
        String json= """
                {
                    "title" : "dsa",
                    "content" : ""
                }
                """;
        mockMvc.perform(
                post("/api/board")
                        .requestAttr("username","tester")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("내용은 필수입니다."));
        verify(boardService, never()).create(any(), anyString());
    }
    @Test
    @DisplayName("게시글_생성_제목_400")
    void createTitleOrContentIsBlank() throws Exception{
        String json= """
                {
                    "title" : "",
                    "content" : "asda"
                }
                """;
        mockMvc.perform(
                        post("/api/board")
                                .requestAttr("username","tester")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목은 필수입니다."));
        verify(boardService, never()).create(any(), anyString());
    }

    @Test
    @DisplayName("게시글_생성_제목_너무_김_400")
    void createTitleToLong()throws Exception{
        String json = """
                {
                    "title" :"jslekrwjrweioruviaowerwrktjqwelkrjtwlkrvjslekrwjrweioruviaowerwrktjqwelkrjtwlkrvjsaklrjewiornvjklsgajksthweiotjwailtjnwaoptvjwalktjaslkgnashnglahwiorjwioeru2iorunsefjaklenjariuerioawuralfkdjsalkfejfklsadjfawilfdlskajf3eiowrjnafhwaiofhawiefhiwhiowvjsaklrjewiornvjklsgajksthweiotjwailtjnwaoptvjwalktjaslkgnashnglahwiorjwioeru2iorunsefjaklenja;riuerioawuralfkdjsalkfejfklsad;jfawilfdlskajf3eiowrjnafhwaiofhawiefhiwhiowv",
                    "content" : "zx"
                }
                """;
        mockMvc.perform(
                post("/api/board")
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
    void createContentToLong()throws Exception{
        String json = """
                {
                    "title" :"jdsdsd",
                     "content" : "slekrwjrwdjwklrjqoirwrkfjwejrlkawejrlaralrhlawerjkbhwriurhuisberh23iurhsaruiarh82rheurasajkfhfauiwfhb289rhwefahf289bfheskhfvawekfh2iufvesfjaklejfewklfjwelfweffahejkrhawekjfhasdkjfhasflehauwifhawueifvlahwfajkhfaklsioruviaowerwrktjqwelkrjtwlkrvjslekrwjrweioruviaowerwrktjqwelkrjtwlkrvjsaklrjewiornvjklsgajksthweiotjwailtjnwaoptvjwalktjaslkgnashnglahwiorjwioeru2iorunsefjaklenjariuerioawuralfkdjsalkfejfklsadjfawilfdlskajf3erjtwlkrvjslekrwjrweioruviaowerwrktjqwelkrjtwlkrvjsaklrjewiornvjklsgajksfjweklr23yr32uirh2rui23rih2o3iurh2buirh23uri23iur23hri2u34y8914yruihfhjksfahlkfjehfkjhfdkjfhkjsfhdsjkfhdsjlkafhdjsalfhdjsaklyweruieowqryeuwoqryeuwqoreyuwqoireyuwqoreywuqoreyuwqoreyuwqoryewuqoreywquoreywrueowqryeuwqoryeuwoqryeuwqoryeuwoqryeuwryeuwoiqryeuwqioryeuwqoryeuwoqiryeuwqoryeuwqoryeuwqoryeuwoqoryeuwqioryeuwqoiryeuwothweiotjwailtjnwaoptvjwalktjaslkgnashnglahwiorjwioeru2iorunsefjaklenjariuerioawuralfkdjsalkfejfklsadjfawilfdlskajf3eiowrjnafhwaiofhawiefhiwhiowvjsaklrjewiornvjklsgajksthweiotjwailtjnwaoptvjwalktjaslkgnashnglahwiorjwioeru2iorunsefjaklenjariuerioawuralfkdjsalkfejfklsadjfawilfdiowrjnafhwaiofhawiefhiwhiowvjsaklrjewiornvjklsgajksthweiotjwailtjnwaoptvjwalktjaslkgnashnglahwiorjwioeru2iorunsefjaklenja;riuerioawuralfkdjsalkfejfklsad;jfawilfdlskajf3eiowrjnafhwaiofhawiefhiwhiowv"
                }
                """;
        mockMvc.perform(
                        post("/api/board")
                                .requestAttr("username", "tester")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("본문은 1000자 미만이어야 합니다"));
        verify(boardService, never()).create(any(), anyString());
    }

    // 존재하지 않은 게시글
    // 이미 사용 중인 제목
    
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
    @DisplayName("게시글 전체 조회")
    void getAll() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("title").ascending());

        BoardSummaryResponse dummy = new BoardSummaryResponse(
                1L,
                10L,
                "제목",
                LocalDateTime.now()
        );

        List<BoardSummaryResponse> content = List.of(dummy);

        PageResponse<BoardSummaryResponse> pageResponse =
                new PageResponse<>(
                        content,   // content
                        0,         // page
                        10,        // size
                        1L         // totalElements
                );

        ApiResponse<PageResponse<BoardSummaryResponse>> body =
                ApiResponse.ok(pageResponse, "게시글 전체 조회!");

        given(boardService.findAll(anyInt(), anyInt()))
                .willReturn(body);

        mockMvc.perform(
                        get(BASE)
                                .param("page", "0")
                                .param("size", "10")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].userId").value(10))
                .andExpect(jsonPath("$.data.content[0].title").value("제목"))
//                .andExpect(jsonPath("$.data.content[0].contents").value("내용"))
                .andExpect(jsonPath("$.data.content[0].createdDate", notNullValue()))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10));

        verify(boardService).findAll(anyInt(), anyInt());
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
                        post(BASE + "/{id}", boardId)
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