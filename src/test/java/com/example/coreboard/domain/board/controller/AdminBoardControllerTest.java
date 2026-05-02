package com.example.coreboard.domain.board.controller;

import com.example.coreboard.domain.board.dto.command.DeleteBoardCommand;
import com.example.coreboard.domain.board.dto.command.UpdateBoardCommand;
import com.example.coreboard.domain.board.dto.request.UpdateBoardRequest;
import com.example.coreboard.domain.board.dto.result.CreateBoardResult;
import com.example.coreboard.domain.board.dto.command.CreateBoardCommand;
import com.example.coreboard.domain.board.dto.request.CreateBoardRequest;
import com.example.coreboard.domain.board.dto.result.UpdateBoardResult;
import com.example.coreboard.domain.board.service.BoardService;
import com.example.coreboard.domain.support.fixture.MockMvcSupport;
import com.example.coreboard.domain.users.entity.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminBoardControllerTest {
    @Mock
    BoardService boardService;
    @InjectMocks
    AdminBoardController boardController;

    MockMvc mockMvc;
    MockMvc mockMvcWithInterceptor;
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcSupport.create(boardController);
        mockMvcWithInterceptor = MockMvcSupport.createWithInterceptor(boardController);
        objectMapper = new ObjectMapper();
    }


    @Test
    @DisplayName("게시판_생성_성공")
    void createBoard() throws Exception {
        // id, name, slug, requireAttachment(첨부파일 필수냐), maxAttachmentCount(첨부파일 몇 개까지), maxContentLength( 본문 최대 길이)
        // answerAcceptedEnabled(답변 채택 허용 여부), commentEnabled(댓글 허용 여부), requiredWriteRole(누가 쓸 수 있냐), active(게시판 사용 여부)
        CreateBoardRequest request = new CreateBoardRequest(
                "자유게시판",
                "free",
                false,
                false,
                false,
                0,
                10000,
                UserRole.USER
        );
        CreateBoardResult result = new CreateBoardResult(1L);

        given(boardService.create(any(CreateBoardCommand.class), eq("username"))).willReturn(result);

        mockMvc.perform(
                        post("/admin/boards")
                                .requestAttr("username", "username")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("성공적으로 게시판이 생성되었습니다."))
                .andExpect(jsonPath("$.data").exists());

        ArgumentCaptor<CreateBoardCommand> captor = ArgumentCaptor.forClass(CreateBoardCommand.class);

        verify(boardService).create(captor.capture(), eq("username"));

        CreateBoardCommand command = captor.getValue();

        assertThat(command.name()).isEqualTo("자유게시판");
        assertThat(command.slug()).isEqualTo("free");
        assertThat(command.requireAttachment()).isFalse();
        assertThat(command.maxAttachmentCount()).isEqualTo(0);
        assertThat(command.maxContentLength()).isEqualTo(10000);
        assertThat(command.answerAcceptedEnabled()).isFalse();
        assertThat(command.commentEnabled()).isFalse();
        assertThat(command.requiredWriteRole()).isEqualTo(UserRole.USER);

        verifyNoMoreInteractions(boardService);
    }

    // TODO : AMDIN 아닌 계정으로 게시판 생성
    @Test
    @DisplayName("게시판_수정_성공")
    void updateBoard() throws Exception {
        String username = "username";
        UpdateBoardRequest request = new UpdateBoardRequest(
                1L,
                "자유게시판",
                "free",
                false,
                false,
                false,
                0,
                10000
        );
        UpdateBoardResult result = new UpdateBoardResult(1L);

        given(boardService.update(any(UpdateBoardCommand.class),eq(username), eq(1L))).willReturn(result);
        mockMvc.perform(
                        patch("/admin/boards/{id}", 1L)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("성공적으로 수정되었습니다."))
                .andExpect(jsonPath("$.data.id").value(1L));
        ArgumentCaptor<UpdateBoardCommand> captor = ArgumentCaptor.forClass(UpdateBoardCommand.class);

        verify(boardService).update(captor.capture(),eq(username), eq(1L));

        UpdateBoardCommand command = captor.getValue();
        assertThat(command.id()).isEqualTo(1L);
        assertThat(command.name()).isEqualTo("자유게시판");
        assertThat(command.slug()).isEqualTo("free");
        assertThat(command.answerAcceptedEnabled()).isFalse();
        assertThat(command.commentEnabled()).isFalse();
        assertThat(command.requireAttachment()).isFalse();
        assertThat(command.maxAttachmentCount()).isEqualTo(0);
        assertThat(command.maxContentLength()).isEqualTo(10000);
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시판_삭제_성공")
    void deleteBoard() throws Exception{
        String username = "username";
        Long id = 1L;
        mockMvc.perform(
                delete("/admin/boards/{id}", id)
                        .requestAttr("username", username))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        ArgumentCaptor<DeleteBoardCommand> captor = ArgumentCaptor.forClass(DeleteBoardCommand.class);
        verify(boardService).delete(captor.capture());
        DeleteBoardCommand command = captor.getValue();

        assertThat(command.id()).isEqualTo(id);
        assertThat(command.username()).isEqualTo(username);
        verifyNoMoreInteractions(boardService);
    }
    // TODO : 비로그인 401 체크 - create, udpate, delete

}