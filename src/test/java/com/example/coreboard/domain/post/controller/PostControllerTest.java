package com.example.coreboard.domain.post.controller;

import com.example.coreboard.domain.comment.dto.response.GetAllCommentResponse;
import com.example.coreboard.domain.common.response.SliceInfo;
import com.example.coreboard.domain.common.response.SliceResponse;
import com.example.coreboard.domain.post.dto.command.DeletePostCommand;
import com.example.coreboard.domain.post.dto.command.GetOnePostCommand;
import com.example.coreboard.domain.post.dto.request.UpdatePostRequest;
import com.example.coreboard.domain.post.dto.result.GetOnePostResult;
import com.example.coreboard.domain.post.dto.result.UpdatePostResult;
import com.example.coreboard.domain.common.type.ContentFormat;
import com.example.coreboard.domain.post.service.PostService;
import com.example.coreboard.domain.common.exception.post.PostErrorCode;
import com.example.coreboard.domain.common.exception.post.PostErrorException;
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

import static org.mockito.ArgumentMatchers.any;
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
    PostService postService;

    @InjectMocks
    PostController postControler;

    MockMvc mockMvc;
    MockMvc mockMvcWithInterceptor;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcSupport.create(postControler);
        mockMvcWithInterceptor = MockMvcSupport.createWithInterceptor(postControler);
    }

    @Test
    @DisplayName("게시글_단건_조회_성공")
    void getOne() throws Exception {
        SliceResponse<GetAllCommentResponse> comments = new SliceResponse<>(
                List.of(),
                new SliceInfo(10, 0, false)
        );
        GetOnePostResult dummy = new GetOnePostResult(
                id,
                userId,
                "제목",
                "본문",
                LocalDateTime.now(),
                LocalDateTime.now(),
                comments,
                List.of()
        );
        given(postService.getOne(any(GetOnePostCommand.class))).willReturn(dummy);
        mockMvc.perform(
                        get(BASE + "/{id}", id)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("게시글 단건 조회!"));

        verify(postService).getOne(any(GetOnePostCommand.class));
        verifyNoMoreInteractions(postService);
    }

    @Test
    @DisplayName("게시글_단건_조회_존재하지_않는_게시글_404")
    void getOneIsNotFoundBoard() throws Exception {

        given(postService.getOne(any(GetOnePostCommand.class)))
                .willThrow(new PostErrorException(PostErrorCode.POST_NOT_FOUND));
        mockMvc.perform(
                        get(BASE + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 게시글입니다."))
                .andExpect(jsonPath("$.data.code").value(404));
        verify(postService).getOne(any(GetOnePostCommand.class));
        verifyNoMoreInteractions(postService);
    }

    @Test
    @DisplayName("게시글_수정")
    void update() throws Exception {
        UpdatePostResult dummy = new UpdatePostResult(id, LocalDateTime.now(), LocalDateTime.now());
        UpdatePostRequest request = new UpdatePostRequest(
                "newTitle",
                "newContent",
                ContentFormat.MARKDOWN,
                List.of(1L),
                List.of(2L)
        );
        String json = objectMapper.writeValueAsString(request);

        given(postService.update(any())).willReturn(dummy);
        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("게시글이 성공적으로 수정되었습니다."));
        verify(postService).update(any());
        verifyNoMoreInteractions(postService);
    }

    @Test
    @DisplayName("게시글_수정_비로그인_401")
    void updateIsUnauthorized() throws Exception {
        UpdatePostRequest request = new UpdatePostRequest(
                "newTitle",
                "newContent",
                ContentFormat.MARKDOWN,
                List.of(1L),
                List.of(2L)
        );
        String json = objectMapper.writeValueAsString(request);
        mockMvcWithInterceptor.perform(
                        put(BASE + "/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("다시 로그인해 주세요."));
        verify(postService, never()).update(any());
    }

    @Test
    @DisplayName("게시글_수정_존재하지_않는_게시글_404")
    void updateNotFound() throws Exception {
        UpdatePostRequest request = new UpdatePostRequest(
                "newTitle",
                "newContent",
                ContentFormat.MARKDOWN,
                List.of(1L),
                List.of(2L)
        );
        String json = objectMapper.writeValueAsString(request);

        given(postService.update(any())).willThrow(new PostErrorException(PostErrorCode.POST_NOT_FOUND));
        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 게시글입니다."));
        verify(postService).update(any());
        verifyNoMoreInteractions(postService);
    }

    @Test
    @DisplayName("게시글_수정_본문_길이_초과_400")
    void updateContentTooLong() throws Exception {
        String content = "a".repeat(100100);
        UpdatePostRequest request = new UpdatePostRequest(
                "newTitle",
                content,
                ContentFormat.MARKDOWN,
                List.of(1L),
                List.of(2L)
        );
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("본문은 10000자 이하여야 합니다."));
        verify(postService, never()).update(any());
    }

    @Test
    @DisplayName("게시글_수정_제목_길이_초과_400")
    void updateTitleTooLong() throws Exception {
        String title = "a".repeat(256);
        UpdatePostRequest request = new UpdatePostRequest(
                title,
                "newContent",
                ContentFormat.MARKDOWN,
                List.of(1L),
                List.of(2L)
        );
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목은 255자 이하여야 합니다."));
        verify(postService, never()).update(any());
    }

    @Test
    @DisplayName("게시글_수정_제목과_본문_비어있음_400")
    void updateTitleAndContentIsBlank() throws Exception {
        UpdatePostRequest request = new UpdatePostRequest(
                "",
                "",
                ContentFormat.MARKDOWN,
                List.of(1L),
                List.of(2L)
        );
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목과 내용은 필수입니다."));
        verify(postService, never()).update(any());
    }

    @Test
    @DisplayName("게시글_수정_제목_비어있음_400")
    void updateContentIsBlank() throws Exception {
        UpdatePostRequest request = new UpdatePostRequest(
                "",
                "content",
                ContentFormat.MARKDOWN,
                List.of(1L),
                List.of(2L)
        );
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목은 필수입니다."));
        verify(postService, never()).update(any());
    }

    @Test
    @DisplayName("게시글_수정_본문_비어있음_400")
    void updateTitleIsBlank() throws Exception {
        UpdatePostRequest request = new UpdatePostRequest(
                "newTitle",
                "",
                ContentFormat.MARKDOWN,
                List.of(1L),
                List.of(2L)
        );
        String json = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("본문은 필수입니다."));
        verify(postService, never()).update(any());
    }

    @Test
    @DisplayName("게시글_수정_이미_삭제된_게시글")
    void updateIsDelete() throws Exception {
        UpdatePostRequest requrest = new UpdatePostRequest(
                "newTitle",
                "newContent",
                ContentFormat.MARKDOWN,
                List.of(1L),
                List.of(2L)
        );
        String json = objectMapper.writeValueAsString(requrest);
        given(postService.update(any())).willThrow(new PostErrorException(PostErrorCode.POST_ISDELETE));
        mockMvc.perform(
                        put(BASE + "/{id}", id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("삭제된 게시글입니다."));
        verify(postService).update(any());
        verifyNoMoreInteractions(postService);
    }

    @Test
    @DisplayName("게시글_삭제")
    void deleted() throws Exception {
        mockMvc.perform(
                        delete(BASE + "/{id}", id)
                                .requestAttr("username", username))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(postService).delete(any(DeletePostCommand.class));
        verifyNoMoreInteractions(postService);
    }

    @Test
    @DisplayName("게시글_삭제_존재하지_않는_게시글_404")
    void deleteNotFound() throws Exception {
        willThrow(new PostErrorException(PostErrorCode.POST_NOT_FOUND))
                .given(postService).delete(any(DeletePostCommand.class));
        mockMvc.perform(
                        delete(BASE + "/{id}", id)
                                .requestAttr("username", username))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("존재하지 않는 게시글입니다."));

        verify(postService).delete(any(DeletePostCommand.class));
        verifyNoMoreInteractions(postService);
    }

    @Test
    @DisplayName("게시글_삭졔_로그인_안함_401")
    void deleteUnauthorized() throws Exception {
        mockMvcWithInterceptor.perform(
                        delete(BASE + "/{id}", id))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("다시 로그인해 주세요."));
        verify(postService, never()).update(any());
    }
}