package com.example.coreboard.domain.post.controller;

import com.example.coreboard.domain.post.dto.command.GetOnePostCommand;
import com.example.coreboard.domain.post.dto.request.CreatePostRequest;
import com.example.coreboard.domain.post.dto.request.UpdatePostRequest;
import com.example.coreboard.domain.post.dto.response.PostSummaryResponse;
import com.example.coreboard.domain.post.dto.result.CreatePostResult;
import com.example.coreboard.domain.post.dto.result.GetOnePostResult;
import com.example.coreboard.domain.post.dto.result.UpdatePostResult;
import com.example.coreboard.domain.post.entity.ContentFormat;
import com.example.coreboard.domain.post.service.PostService;
import com.example.coreboard.domain.common.exception.auth.AuthErrorCode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.post.PostErrorCode;
import com.example.coreboard.domain.common.exception.post.PostErrorException;
import com.example.coreboard.domain.common.response.CursorResponse;
import com.example.coreboard.domain.support.fixture.MockMvcSupport;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

import org.springframework.test.web.servlet.MockMvc;

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
class PostControllerTest {
    private static final String BASE = "/posts";
    ObjectMapper objectMapper = new ObjectMapper();
    String username = "tester";
    long id = 1;
    long userId = 1;

    @Mock
    PostService boardService;

    @InjectMocks
    PostController boardController;

    MockMvc mockMvc;
    MockMvc mockMvcWithInterceptor;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcSupport.create(boardController);
        mockMvcWithInterceptor = MockMvcSupport.createWithInterceptor(boardController);
    }

    @Test
    @DisplayName("게시글_생성")
    void create() throws Exception {
        String username = "tester";
        CreatePostResult dummy = new CreatePostResult(id);
        given(boardService.create(any(), eq(username))).willReturn(dummy);

        CreatePostRequest request = new CreatePostRequest(
                1L, "title", "content", ContentFormat.MARKDOWN);

        mockMvc.perform(
                        post(BASE)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("게시글이 성공적으로 생성되었습니다."))
                .andExpect(jsonPath("$.data.id").value(1));
        verify(boardService).create(any(), eq(username));
    }

    @Test
    @DisplayName("게시글_생성_유저없음_404")
    void createIsNotUser() throws Exception {
        CreatePostRequest request = new CreatePostRequest(
                1L, "title", "content", ContentFormat.MARKDOWN);

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
        CreatePostRequest request = new CreatePostRequest(
                1L, "title", "content", ContentFormat.MARKDOWN);
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

        CreatePostRequest request = new CreatePostRequest(1L, "titile", "content", ContentFormat.MARKDOWN);
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
        CreatePostRequest request = new CreatePostRequest(1L, "", "", ContentFormat.MARKDOWN);
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
        CreatePostRequest request = new CreatePostRequest(1L, "title", "", ContentFormat.MARKDOWN);
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
        CreatePostRequest request = new CreatePostRequest(1L, "", "content", ContentFormat.MARKDOWN);
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
        CreatePostRequest request = new CreatePostRequest(1L, longTitle, "zx", ContentFormat.MARKDOWN);
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
        CreatePostRequest request = new CreatePostRequest(1L, "title", longContent, ContentFormat.MARKDOWN);
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
        GetOnePostResult dummy = new GetOnePostResult(
                id,
                userId,
                "제목",
                "본문",
                LocalDateTime.now(),
                LocalDateTime.now());

        given(boardService.getOne(any(GetOnePostCommand.class))).willReturn(dummy);
        mockMvc.perform(
                        get(BASE + "/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("게시글 단건 조회!"));

        verify(boardService).getOne(any(GetOnePostCommand.class));

        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_단건_조회_존재하지_않는_게시글_404")
    void getOneIsNotFoundBoard() throws Exception {

        given(boardService.getOne(any(GetOnePostCommand.class)))
                .willThrow(new PostErrorException(PostErrorCode.POST_NOT_FOUND));
        mockMvc.perform(
                        get(BASE + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 게시글입니다."))
                .andExpect(jsonPath("$.data.code").value(404));
        verify(boardService).getOne(any(GetOnePostCommand.class));
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_전체_조회")
    void getAll() throws Exception {
        List<PostSummaryResponse> items = List.of(
                new PostSummaryResponse(1L, "nickname", "title", LocalDateTime.now(), LocalDateTime.now()));
        CursorResponse<PostSummaryResponse> cursorResponse = new CursorResponse<>(items, null, null, false);
        given(boardService.getAll(null, null, 10, "desc")).willReturn(cursorResponse);
        mockMvc.perform(
                        get(BASE)
                                .param("size", "10")
                                .param("sort", "desc")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글 전체 조회!"))
                .andExpect(jsonPath("$.data.contents[0].id").value(1))
                .andExpect(jsonPath("$.data.contents[0].writerName").value("nickname"))
                .andExpect(jsonPath("$.data.contents[0].title").value("title"))
                .andExpect(jsonPath("$.data.hasNext").value(false));

        verify(boardService).getAll(null, null, 10, "desc");
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_전체_조회_Size_10_이상_400")
    void getAllSizeTooLonger() throws Exception {
        mockMvc.perform(
                        get(BASE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("size", "11")
                                .param("sort", "desc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("size는 최대 10이하이어야 합니다."));
        verify(boardService, never()).getAll(anyString(), anyLong(), anyInt(), anyString());
    }

    @Test
    @DisplayName("게시글_전체조회_size_0")
    void getAllShortSize() throws Exception {
        mockMvc.perform(
                        get(BASE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("size", "0")
                                .param("sort", "asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("size는 최대 10이하이어야 합니다."));
        verify(boardService, never()).getAll(anyString(), anyLong(), anyInt(), anyString());
    }

    @Test
    @DisplayName("게시글_전체조회_size_11")
    void getAllTooLongsize() throws Exception {
        mockMvc.perform(
                        get(BASE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("size", "11")
                                .param("sort", "desc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("size는 최대 10이하이어야 합니다."));
        verify(boardService, never()).getAll(anyString(), anyLong(), anyInt(), anyString());
    }

    @Test
    @DisplayName("게시글_전체_조회_desc_정상")
    void getAllDesc() throws Exception {
        List<PostSummaryResponse> items = List.of(
                new PostSummaryResponse(10L, "nickname", "title", LocalDateTime.now(), LocalDateTime.now()));
        CursorResponse<PostSummaryResponse> cursorResponse = new CursorResponse<>(items, null, null,
                false);
        given(boardService.getAll("title", 10L, 10, "desc")).willReturn(cursorResponse);
        mockMvc.perform(
                        get(BASE)
                                .param("cursorTitle", "title")
                                .param("cursorId", "10")
                                .param("size", "10")
                                .param("sort", "desc")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글 전체 조회!"));

        verify(boardService).getAll("title", 10L, 10, "desc");
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
        verify(boardService, never()).getAll(anyString(), anyLong(), anyInt(), anyString());
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_수정")
    void update() throws Exception {
        UpdatePostResult dummy = new UpdatePostResult(id, LocalDateTime.now(), LocalDateTime.now());
        UpdatePostRequest request = new UpdatePostRequest("newTitle", "newContent", ContentFormat.MARKDOWN);
        String json = objectMapper.writeValueAsString(request);

        given(boardService.update(any())).willReturn(dummy);
        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("게시글이 성공적으로 수정되었습니다."));

        verify(boardService).update(any());
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글_수정_비로그인_401")
    void updateIsUnauthorized() throws Exception {
        UpdatePostRequest reqeust = new UpdatePostRequest("newTitle", "newContent", ContentFormat.MARKDOWN);
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
        UpdatePostRequest request = new UpdatePostRequest("newTitle", "newContent", ContentFormat.MARKDOWN);
        String json = objectMapper.writeValueAsString(request);

        given(boardService.update(any())).willThrow(new PostErrorException(PostErrorCode.POST_NOT_FOUND));
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
        UpdatePostRequest request = new UpdatePostRequest("newTitle", "newContent", ContentFormat.MARKDOWN);
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
        UpdatePostRequest request = new UpdatePostRequest("newTitle", content, ContentFormat.MARKDOWN);
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
        UpdatePostRequest request = new UpdatePostRequest(title, "newContent", ContentFormat.MARKDOWN);
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
        UpdatePostRequest request = new UpdatePostRequest("", "", ContentFormat.MARKDOWN);
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
        UpdatePostRequest request = new UpdatePostRequest("", "content", ContentFormat.MARKDOWN);
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
        UpdatePostRequest request = new UpdatePostRequest("newTitle", "", ContentFormat.MARKDOWN);
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
        UpdatePostRequest requrest = new UpdatePostRequest("newTitle", "newContent", ContentFormat.MARKDOWN);
        String json = objectMapper.writeValueAsString(requrest);
        given(boardService.update(any())).willThrow(new PostErrorException(PostErrorCode.POST_ISDELETE));
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
                .andExpect(jsonPath("$.message").value("게시글이 성공적으로 삭제되었습니다."));
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