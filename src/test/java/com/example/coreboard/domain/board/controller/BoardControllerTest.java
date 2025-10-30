package com.example.coreboard.domain.board.controller;

import com.example.coreboard.domain.board.dto.*;
import com.example.coreboard.domain.board.service.BoardService;
import com.example.coreboard.domain.common.exception.GlobalExceptionHandler;
import com.example.coreboard.domain.common.exception.auth.AuthErrorCode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.board.BoardErrorCode;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;
import com.example.coreboard.domain.common.interceptor.AuthInterceptor;
import com.example.coreboard.domain.common.response.ApiResponse;
import com.example.coreboard.domain.common.response.PageResponse;
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

import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
@Import(GlobalExceptionHandler.class)
class BoardControllerTest {
    private static final String BASE = "/board";
    String username = "tester";
    long id = 1;
    long userId = 1;

    @Mock
            BoardService boardService;

    @InjectMocks
    BoardController boardController;

    MockMvc mockMvc;
    MockMvc mockMvcWithInterceptor;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(boardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();

        mockMvcWithInterceptor = MockMvcBuilders
                .standaloneSetup(boardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .addInterceptors(new AuthInterceptor())
                .build();
    }

    @Test
    @DisplayName("게시글_생성")
    void create() throws Exception {
        String username = "tester";

        BoardCreateDto dummy = new BoardCreateDto(
                id,
                userId,
                "제목",
                "본문",
                LocalDateTime.now()
        );
        given(boardService.create(any(), eq(username))).willReturn(dummy);

        String json = """
                {
                    "title" : "제목",
                    "content" : "본문"
                }
                """;

        mockMvc.perform(
                        post(BASE)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("게시글이 성공적으로 생성되었습니다."))
                .andExpect(jsonPath("$.data.title").value("제목"))
                .andExpect(jsonPath("$.data.content").value("본문"))
                .andExpect(jsonPath("$.data.createdDate", notNullValue()));
        verify(boardService).create(any(), eq(username));
    }

    @Test
    @DisplayName("게시글_생성_유저없음_404")
    void createIsNotUser() throws Exception {
        String json = """
                {
                  "title" : "제목",
                  "content" : "내용"
                }
                """;

        given(boardService.create(any(), eq("ghost")))
                .willThrow(new AuthErrorException(AuthErrorCode.NOT_FOUND));

        mockMvc.perform(
                        post(BASE)
                                .requestAttr("username", "ghost")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));

        verify(boardService).create(any(), eq("ghost"));
        verifyNoMoreInteractions(boardService);
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

        given(boardService.create(any(), eq(username))).willThrow(new AuthErrorException(AuthErrorCode.FORBIDDEN));
        mockMvc.perform(
                        post(BASE)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));

        verify(boardService).create(any(), eq(username));
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_생성_로그인_안함_401")
    void createUnauthorized() throws Exception {

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

        verify(boardService, never()).create(any(), anyString());
    }

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
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목과 내용은 필수입니다."));
        verify(boardService, never()).create(any(), anyString());
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
                                .requestAttr("username", username)
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
                                .requestAttr("username", username)
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
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목은 255자 이하여야 합니다."));

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
                .andExpect(jsonPath("$.message").value("내용은 1000자 이하여야 합니다."));

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

        given(boardService.create(any(), eq(username))).willThrow(new BoardErrorException(BoardErrorCode.TITLE_DUPLICATED));

        mockMvc.perform(
                        post(BASE)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 사용 중인 제목입니다."));

        verify(boardService).create(any(), eq(username));
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_단건_조회_성공")
    void getOne() throws Exception {
        BoardGetOneDto dummy = new BoardGetOneDto(
                id,
                userId,
                "제목",
                "본문",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(boardService.findOne(any(BoardGetOneCommand.class))).willReturn(dummy);

        mockMvc.perform(
                        get(BASE + "/{id}", id)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("게시글 단건 조회!"));

        verify(boardService).findOne(any(BoardGetOneCommand.class));

        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_단건_조회_존재하지_않는_게시글_404")
    void getOneIsNotFoundBoard() throws Exception {

        given(boardService.findOne(any(BoardGetOneCommand.class))).willThrow(new BoardErrorException(BoardErrorCode.POST_NOT_FOUND));

        mockMvc.perform(
                        get(BASE + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 게시글입니다."))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(boardService).findOne(any(BoardGetOneCommand.class));
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_전체_조회")
    void getAll() throws Exception {
        BoardSummaryResponse item = new BoardSummaryResponse(
                1L, 10L, "제목", LocalDateTime.now()
        );

        PageResponse<BoardSummaryResponse> pageResponse = new PageResponse<>(List.of(item), 0, 10, 1L);
        PageResponse<BoardSummaryResponse> body = ApiResponse.ok(pageResponse, "게시글 전체 조회!").getData();

        given(boardService.findAll(eq(0), eq(10), eq("asc"))).willReturn(body);
        mockMvc.perform(
                        get(BASE)
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "asc")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글 전체 조회!"))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].userId").value(10))
                .andExpect(jsonPath("$.data.content[0].title").value("제목"))
                .andExpect(jsonPath("$.data.content[0].createdDate", notNullValue()))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10));
        verify(boardService).findAll(eq(0), eq(10), eq("asc"));
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_전체_조회_Page_0_이상_400")
    void getAllPageNegatice() throws Exception {
        mockMvc.perform(
                        get(BASE)
                                .param("page", "-1")
                                .param("size", "10")
                                .accept(MediaType.APPLICATION_JSON)
                )

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("page는 0이상이어야 합니다."));
        verify(boardService, never()).findAll(anyInt(), anyInt(), anyString());
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
        verify(boardService, never()).findAll(anyInt(), anyInt(), anyString());
    }

    @Test
    @DisplayName("게시글_전체조회_size_0")
    void getAllShortSize()throws Exception{
        mockMvc.perform(
                get(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page","0")
                        .param("size","0")
                        .param("sort","asc")

        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("zise는 최대 10이하이어야 합니다"));
        verify(boardService, never()).findAll(anyInt(), anyInt(), anyString());
    }

    @Test
    @DisplayName("게시글_전체조회_size_11")
    void getAllTooLongsize()throws Exception{
        mockMvc.perform(
                        get(BASE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("page","0")
                                .param("size","11")
                                .param("sort","asc")

                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("zise는 최대 10이하이어야 합니다"));
        verify(boardService, never()).findAll(anyInt(), anyInt(), anyString());
    }

    @Test
    @DisplayName("게시글_전체조회_sort_not_asc")
    void getAllNotAsc()throws Exception{
        mockMvc.perform(
                        get(BASE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("page","0")
                                .param("size","10")
                                .param("sort","aaa")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("정렬 방향은 asc 또는 desc만 허용됩니다."));
        verify(boardService, never()).findAll(anyInt(), anyInt(), anyString());
    }

    @Test
    @DisplayName("게시글_전체_조회_desc_정상")
    void getAllDesc() throws Exception {
        BoardSummaryResponse item = new BoardSummaryResponse(
                1L, 10L, "제목", LocalDateTime.now()
        );

        PageResponse<BoardSummaryResponse> pageResponse =
                new PageResponse<>(List.of(item), 0, 10, 1L);

        PageResponse<BoardSummaryResponse> body =
                ApiResponse.ok(pageResponse, "게시글 전체 조회!").getData();

        given(boardService.findAll(eq(0), eq(10), eq("desc"))).willReturn(body);

        mockMvc.perform(
                        get(BASE)
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "desc")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글 전체 조회!"));

        verify(boardService).findAll(eq(0), eq(10), eq("desc"));
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_전체_조회_정렬_방향_잘못됨_404")
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
        verify(boardService, never()).findAll(anyInt(), anyInt(), anyString());
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_수정")
    void update() throws Exception {
        BoardUpdatedDto dummy = new BoardUpdatedDto(
                id
        );
        String json = """
                {
                    "title" : "제목",
                    "content" : "내용"
                }
                """;

        given(boardService.update(any())).willReturn(dummy);

        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("게시글 수정 완료!"));

        verify(boardService).update(any());
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_수정_비로그인_401")
    void updateIsUnauthorized() throws Exception {
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
        verify(boardService, never()).update(any());
    }

    @Test
    @DisplayName("게시글_수정_존재하지_않는_게시글_404")
    void updateNotFound() throws Exception {
        String json = """
                {
                    "title" : "제목",
                    "content" : "본문"
                }
                """;

        given(boardService.update(any())).willThrow(new BoardErrorException(BoardErrorCode.POST_NOT_FOUND));

        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 게시글입니다."));
        verify(boardService).update(any());
        verifyNoMoreInteractions(boardService);

    }

    @Test
    @DisplayName("게시글_수정_403")
    void updateForbidden() throws Exception {
        String json = """
                {
                    "title" : "제목",
                    "content" : "본문"
                }
                """;
        given(boardService.update(any())).willThrow(new AuthErrorException(AuthErrorCode.FORBIDDEN));

        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
        verify(boardService).update(any());
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_수정_본문_길이_초과_400")
    void updateContentTooLong() throws Exception {
        String content = "a".repeat(1001);
        String json = String.format("""
                {
                    "title" : "fds",
                    "content" : "%s"
                }
                """, content);
        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("내용은 1000자 이하여야 합니다."));
        verify(boardService, never()).update(any());
    }

    @Test
    @DisplayName("게시글_수정_제목_길이_초과_400")
    void updateTitleTooLong() throws Exception {
        String title = "a".repeat(256);
        String json = String.format("""
                {
                    "title" : "%s",
                    "content" : "tiae"
                }
                """, title);
        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목은 255자 이하여야 합니다."));
        verify(boardService, never()).update(any());
    }

    @Test
    @DisplayName("게시글_수정_제목과_본문_비어있음_400")
    void updateTitleAndContentIsBlank() throws Exception {
        String json = """
                {
                    "titlie" : "",
                    "content" : ""
                }
                """;
        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목과 내용은 필수입니다."));
        verify(boardService, never()).update(any());
    }

    @Test
    @DisplayName("게시글_수정_제목_비어있음_400")
    void updateContentIsBlank() throws Exception {
        String json = """
                {
                    "title" : "",
                    "content" : "sta"
                }
                """;
        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목은 필수입니다."));
        verify(boardService, never()).update(any());
    }

    @Test
    @DisplayName("게시글_수정_본문_비어있음_400")
    void updateTitleIsBlank() throws Exception {
        String json = """
                {
                    "title" : "dasda",
                    "content" :""
                }
                """;
        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("내용은 필수입니다."));
        verify(boardService, never()).update(any());
    }

    @Test
    @DisplayName("게시글_수정_이미_삭제된_게시글")
    void updateIsDelete() throws Exception {
        String json = """
                {
                    "title" : "fd",
                    "content" : "wef"
                }
                """;
        given(boardService.update(any())).willThrow(new BoardErrorException(BoardErrorCode.POST_ISDELETE));

        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("삭제된 게시글입니다."));
        verify(boardService).update(any());
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_삭제")
    void deleted() throws Exception {

        mockMvc.perform(
                        delete(BASE + "/{id}", id)
                                .requestAttr("username", username)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("게시글 삭제완료!"));
        verify(boardService).delete(eq(username), eq(id));
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_삭졔_로그인_안함_401")
    void deleteUnauthorized() throws Exception {
        mockMvcWithInterceptor.perform(
                        delete(BASE + "/{id}", id)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("다시 로그인해 주세요."));
        verify(boardService, never()).update(any());
    }

    @Test
    @DisplayName("게시글_삭제_다른_유저_403")
    void deleteForbidden() throws Exception {
        String otherUser = "tester";

        willThrow(new AuthErrorException(AuthErrorCode.FORBIDDEN))
                .given(boardService).delete(eq(otherUser), eq(id));

        mockMvc.perform(
                        delete(BASE + "/{id}", id)
                                .requestAttr("username", otherUser)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."))
        ;
        verify(boardService).delete(eq(username), eq(id));
        verifyNoMoreInteractions(boardService);
    }
}