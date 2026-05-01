package com.example.coreboard.domain.board.controller;

import com.example.coreboard.domain.board.dto.CreateBoardDto;
import com.example.coreboard.domain.board.dto.GetOneBoardDto;
import com.example.coreboard.domain.board.dto.command.CreateBoardCommand;
import com.example.coreboard.domain.board.dto.command.GetOneBoardCommand;
import com.example.coreboard.domain.board.dto.request.CreateBoardRequest;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BoardControllerTest {
    @Mock
    BoardService boardService;
    @InjectMocks
    BoardController boardController;

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
        CreateBoardDto result = new CreateBoardDto(1L);

        given(boardService.create(any(CreateBoardCommand.class), eq("username"))).willReturn(result);

        mockMvc.perform(
                        post("/admin/boards")
                                .requestAttr("username", "username")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
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

    @Test
    @DisplayName("게시판_단건조회_성공")
    void getOneBoard() throws Exception {
        GetOneBoardDto out = new GetOneBoardDto(
                1L,
                "자유게시판",
                "free",
                false,
                false,
                false,
                0,
                10000,
                UserRole.USER,
                List.of()
        );
        given(boardService.getOne(any(GetOneBoardCommand.class)))
                .willReturn(out);
        mockMvc.perform(
                        get("/admin/boards/{boardId}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("성공적으로 불러왔습니다."))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("자유게시판"))
                .andExpect(jsonPath("$.data.slug").value("free"))
                .andExpect(jsonPath("$.data.requiredWriteRole").value("USER"))
                .andExpect(jsonPath("$.data.posts").isArray());
        ;

        ArgumentCaptor<GetOneBoardCommand> captor =
                ArgumentCaptor.forClass(GetOneBoardCommand.class);

        verify(boardService).getOne(captor.capture());

        GetOneBoardCommand command = captor.getValue();

        assertThat(command.id()).isEqualTo(1L);
        verifyNoMoreInteractions(boardService);
    }

}