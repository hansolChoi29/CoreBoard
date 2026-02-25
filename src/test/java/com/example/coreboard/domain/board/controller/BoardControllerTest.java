package com.example.coreboard.domain.board.controller;

import com.example.coreboard.domain.board.dto.*;
import com.example.coreboard.domain.board.dto.command.BoardGetOneCommand;
import com.example.coreboard.domain.board.dto.request.BoardCreateRequest;
import com.example.coreboard.domain.board.dto.request.BoardUpdateRequest;
import com.example.coreboard.domain.board.dto.response.BoardSummaryKeysetResponse;
import com.example.coreboard.domain.board.service.BoardService;
import com.example.coreboard.domain.common.exception.GlobalExceptionHandler;
import com.example.coreboard.domain.common.exception.auth.AuthErrorCode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.board.BoardErrorCode;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;
import com.example.coreboard.domain.common.interceptor.AuthInterceptor;
import com.example.coreboard.domain.common.response.CursorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@Import(GlobalExceptionHandler.class)
class BoardControllerTest {
    ObjectMapper objectMapper = new ObjectMapper();
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
                LocalDateTime.now());
        given(boardService.create(any(), eq(username))).willReturn(dummy);

        BoardCreateRequest request = new BoardCreateRequest("title", "content");

        mockMvc.perform(
                        post(BASE)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))

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
        BoardCreateRequest request = new BoardCreateRequest("title", "content");

        given(boardService.create(any(), eq("ghost")))
                .willThrow(new AuthErrorException(AuthErrorCode.NOT_FOUND));

        mockMvc.perform(
                        post(BASE)
                                .requestAttr("username", "ghost")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));

        verify(boardService).create(any(), eq("ghost"));
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_생성_권한없음_403")
    void createForbidden() throws Exception {
        BoardCreateRequest request = new BoardCreateRequest("title", "content");

        given(boardService.create(any(), eq(username)))
                .willThrow(new AuthErrorException(AuthErrorCode.FORBIDDEN));
        mockMvc.perform(
                        post(BASE)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));

        verify(boardService).create(any(), eq(username));
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_생성_로그인_안함_401")
    void createUnauthorized() throws Exception {

        BoardCreateRequest request = new BoardCreateRequest("title", "content");

        mockMvcWithInterceptor.perform(
                        post(BASE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("다시 로그인해 주세요."));

        verify(boardService, never()).create(any(), anyString());
    }

    @Test
    @DisplayName("게시글_생성_제목과_본문_비어있음_400")
    void creatteTitleAndContentIsBlank() throws Exception {
        BoardCreateRequest request = new BoardCreateRequest("", "");

        mockMvc.perform(
                        post(BASE)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목과 내용은 필수입니다."));
        verify(boardService, never()).create(any(), anyString());
    }

    @Test
    @DisplayName("게시글_생성_제목_400")
    void createContentIsBlank() throws Exception {
        BoardCreateRequest request = new BoardCreateRequest("title", "");

        mockMvc.perform(
                        post(BASE)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("내용은 필수입니다."));

        verify(boardService, never()).create(any(), anyString());
    }

    @Test
    @DisplayName("게시글_생성_제목_400")
    void createTitleOrContentIsBlank() throws Exception {
        BoardCreateRequest request = new BoardCreateRequest("", "content");

        mockMvc.perform(
                        post(BASE)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목은 필수입니다."));

        verify(boardService, never()).create(any(), anyString());
    }

    @Test
    @DisplayName("게시글_생성_제목_너무_김_400")
    void createTitleToLong() throws Exception {
        String longTitle = "a".repeat(256);
        BoardCreateRequest request = new BoardCreateRequest(longTitle, "zx");
        mockMvc.perform(
                        post(BASE)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목은 255자 이하여야 합니다."));

        verify(boardService, never()).create(any(), anyString());
    }

    @Test
    @DisplayName("게시글_생성_본문_너무_김_400")
    void createContentToLong() throws Exception {
        String longContent = "a".repeat(1001);
        BoardCreateRequest request = new BoardCreateRequest("title", longContent);

        mockMvc.perform(
                        post(BASE)
                                .requestAttr("username", "tester")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("내용은 1000자 이하여야 합니다."));

        verify(boardService, never()).create(any(), anyString());
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
                LocalDateTime.now());

        given(boardService.findOne(any(BoardGetOneCommand.class))).willReturn(dummy);

        mockMvc.perform(
                        get(BASE + "/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("게시글 단건 조회!"));

        verify(boardService).findOne(any(BoardGetOneCommand.class));

        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_단건_조회_존재하지_않는_게시글_404")
    void getOneIsNotFoundBoard() throws Exception {

        given(boardService.findOne(any(BoardGetOneCommand.class)))
                .willThrow(new BoardErrorException(BoardErrorCode.POST_NOT_FOUND));

        mockMvc.perform(
                        get(BASE + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 게시글입니다."))
                .andExpect(jsonPath("$.data.code").value(404));
        verify(boardService).findOne(any(BoardGetOneCommand.class));
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_전체_조회")
    void getAll() throws Exception {
        List<BoardSummaryKeysetResponse> items = List.of(
                new BoardSummaryKeysetResponse(1L, 10L, "title", LocalDateTime.of(2025, 1, 1, 0, 0)));
        CursorResponse<BoardSummaryKeysetResponse> cursorResponse = new CursorResponse<>(items, null, null,
                false);

        given(boardService.findAll(null, null, 10, "asc")).willReturn(cursorResponse);

        mockMvc.perform(
                        get(BASE)
                                .param("size", "10")
                                .param("sort", "asc")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글 전체 조회!"))
                .andExpect(jsonPath("$.data.contents[0].id").value(1))
                .andExpect(jsonPath("$.data.contents[0].userId").value(10))
                .andExpect(jsonPath("$.data.contents[0].title").value("title"))
                .andExpect(jsonPath("$.data.hasNext").value(false));
        verify(boardService).findAll(null, null, 10, "asc");
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_전체_조회_Size_10_이상_400")
    void getAllSizeTooLonger() throws Exception {
        mockMvc.perform(
                        get(BASE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("size", "11")
                                .param("sort", "asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("size는 최대 10이하이어야 합니다."));
        verify(boardService, never()).findAll(anyString(), anyLong(), anyInt(), anyString());
    }

    @Test
    @DisplayName("게시글_전체조회_size_0")
    void getAllShortSize() throws Exception {
        mockMvc.perform(
                        get(BASE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("size", "0")
                                .param("sort", "asc")

                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("size는 최대 10이하이어야 합니다."));
        verify(boardService, never()).findAll(anyString(), anyLong(), anyInt(), anyString());
    }

    @Test
    @DisplayName("게시글_전체조회_size_11")
    void getAllTooLongsize() throws Exception {
        mockMvc.perform(
                        get(BASE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("size", "11")
                                .param("sort", "asc")

                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("size는 최대 10이하이어야 합니다."));
        verify(boardService, never()).findAll(anyString(), anyLong(), anyInt(), anyString());
    }

    @Test
    @DisplayName("게시글_전체_조회_desc_정상")
    void getAllDesc() throws Exception {
        List<BoardSummaryKeysetResponse> items = List.of(
                new BoardSummaryKeysetResponse(10L, 11L, "title", LocalDateTime.of(2025, 1, 1, 0, 0)));
        CursorResponse<BoardSummaryKeysetResponse> cursorResponse = new CursorResponse<>(items, null, null,
                false);
        given(boardService.findAll("title", 10L, 10, "asc")).willReturn(cursorResponse);

        mockMvc.perform(
                        get(BASE)
                                .param("cursorTitle", "title")
                                .param("cursorId", "10")
                                .param("size", "10")
                                .param("sort", "asc")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글 전체 조회!"));

        verify(boardService).findAll("title", 10L, 10, "asc");
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_전체_조회_정렬_방향_잘못됨_404")
    void getAllInvalidSortDirection() throws Exception {
        mockMvc.perform(
                        get(BASE)
                                .param("size", "10")
                                .param("sort", "wrong")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("정렬 방향은 asc 또는 desc만 허용됩니다."));
        verify(boardService, never()).findAll(anyString(), anyLong(), anyInt(), anyString());
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_수정")
    void update() throws Exception {
        BoardUpdatedDto dummy = new BoardUpdatedDto(
                id);
        BoardUpdateRequest request = new BoardUpdateRequest("newTitle", "newContent");
        String json = objectMapper.writeValueAsString(request);

        given(boardService.update(any())).willReturn(dummy);

        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("게시글 수정 완료!"));

        verify(boardService).update(any());
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_수정_비로그인_401")
    void updateIsUnauthorized() throws Exception {
        BoardUpdateRequest reqeust = new BoardUpdateRequest("newTitle", "newContent");
        String json = objectMapper.writeValueAsString(reqeust);

        mockMvcWithInterceptor.perform(
                        put(BASE + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("다시 로그인해 주세요."));
        verify(boardService, never()).update(any());
    }

    @Test
    @DisplayName("게시글_수정_존재하지_않는_게시글_404")
    void updateNotFound() throws Exception {
        BoardUpdateRequest request = new BoardUpdateRequest("newTitle", "newContent");
        String json = objectMapper.writeValueAsString(request);

        given(boardService.update(any())).willThrow(new BoardErrorException(BoardErrorCode.POST_NOT_FOUND));

        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 게시글입니다."));
        verify(boardService).update(any());
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_수정_403")
    void updateForbidden() throws Exception {
        BoardUpdateRequest request = new BoardUpdateRequest("newTitle", "newContent");
        String json = objectMapper.writeValueAsString(request);
        given(boardService.update(any())).willThrow(new AuthErrorException(AuthErrorCode.FORBIDDEN));

        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
        verify(boardService).update(any());
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_수정_본문_길이_초과_400")
    void updateContentTooLong() throws Exception {
        String content = "a".repeat(1001);
        BoardUpdateRequest request = new BoardUpdateRequest("newTitle", content);
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("내용은 1000자 이하여야 합니다."));
        verify(boardService, never()).update(any());
    }

    @Test
    @DisplayName("게시글_수정_제목_길이_초과_400")
    void updateTitleTooLong() throws Exception {
        String title = "a".repeat(256);
        BoardUpdateRequest request = new BoardUpdateRequest(title, "newContent");
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목은 255자 이하여야 합니다."));
        verify(boardService, never()).update(any());
    }

    @Test
    @DisplayName("게시글_수정_제목과_본문_비어있음_400")
    void updateTitleAndContentIsBlank() throws Exception {
        BoardUpdateRequest request = new BoardUpdateRequest("", "");
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목과 내용은 필수입니다."));
        verify(boardService, never()).update(any());
    }

    @Test
    @DisplayName("게시글_수정_제목_비어있음_400")
    void updateContentIsBlank() throws Exception {
        BoardUpdateRequest request = new BoardUpdateRequest("", "content");
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목은 필수입니다."));
        verify(boardService, never()).update(any());
    }

    @Test
    @DisplayName("게시글_수정_본문_비어있음_400")
    void updateTitleIsBlank() throws Exception {
        BoardUpdateRequest request = new BoardUpdateRequest("newTitle", "");
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("내용은 필수입니다."));
        verify(boardService, never()).update(any());
    }

    @Test
    @DisplayName("게시글_수정_이미_삭제된_게시글")
    void updateIsDelete() throws Exception {
        BoardUpdateRequest requrest = new BoardUpdateRequest("newTitle", "newContent");
        String json = objectMapper.writeValueAsString(requrest);
        given(boardService.update(any())).willThrow(new BoardErrorException(BoardErrorCode.POST_ISDELETE));

        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
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
                                .requestAttr("username", username))
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
                        delete(BASE + "/{id}", id))
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
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));
        verify(boardService).delete(eq(username), eq(id));
        verifyNoMoreInteractions(boardService);
    }
}