package com.example.coreboard.domain.comment.controller;

import com.example.coreboard.domain.comment.dto.command.CommentCommand;
import com.example.coreboard.domain.comment.dto.request.CommentRequest;
import com.example.coreboard.domain.comment.dto.result.CommentResult;
import com.example.coreboard.domain.comment.service.CommentService;
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
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {
    String username = "username";
    String BASE = "/posts/{postId}/comments";
    ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    CommentService commentService;

    @InjectMocks
    CommentController commentController;

    MockMvc mockMvc;
    MockMvc mockMvcWithInterceptor;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcSupport.create(commentController);
        mockMvcWithInterceptor = MockMvcSupport.createWithInterceptor(commentController);
    }

    @Test
    @DisplayName("댓글_생성_성공")
    void create() throws Exception {
        CommentRequest request = new CommentRequest("content");
        CommentResult result = new CommentResult(1L);
        given(commentService.create(anyLong(), anyString(), any(CommentCommand.class))).willReturn(result);
        mockMvc.perform(
                        post(BASE, 1L)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("댓글이 성공적으로 작성되었습니다."));
        verify(commentService).create(anyLong(), anyString(), any(CommentCommand.class));
        verifyNoMoreInteractions(commentService);
    }

    @Test
    @DisplayName("댓글수정_성공")
    void update() throws Exception {
        Long postId = 1L;
        Long id = 1L;
        String username = "username";

        CommentRequest request = new CommentRequest("수정");
        CommentResult result = new CommentResult(id);
        given(commentService.update(anyString(), anyLong(), anyLong(), any())).willReturn(result);
        mockMvc.perform(
                        patch(BASE + "/{id}", postId, id)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("성공적으로 수정되었습니다."));
        verify(commentService).update(anyString(), anyLong(), anyLong(), any());
        verifyNoMoreInteractions(commentService);
    }

    @Test
    @DisplayName("댓글삭제_성공")
    void deleteComment() throws Exception {
        Long postId = 1L;
        Long commentId = 10L;
        String username = "username";

        mockMvc.perform(delete(BASE +"/{id}", postId, commentId)
                        .requestAttr("username", username))
                .andExpect(status().isNoContent());

        verify(commentService).delete(postId, commentId, username);
    }
}