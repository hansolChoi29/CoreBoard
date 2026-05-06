package com.example.coreboard.domain.board.controller;

import com.example.coreboard.domain.board.dto.command.GetOneBoardCommand;
import com.example.coreboard.domain.board.dto.query.GetBoardListQuery;
import com.example.coreboard.domain.board.dto.response.GetBoardListResponse;
import com.example.coreboard.domain.board.dto.result.GetOneBoardResult;
import com.example.coreboard.domain.board.service.BoardService;
import com.example.coreboard.domain.common.response.OffsetPageResponse;
import com.example.coreboard.domain.common.response.PageInfo;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    @DisplayName("게시판_단건조회_성공")
    void getOneBoard() throws Exception {
        GetOneBoardResult out = new GetOneBoardResult(
                1L,
                "자유게시판",
                "free",
                false,
                false,
                false,
                0,
                UserRole.USER,
                List.of()
        );
        given(boardService.getOne(any(GetOneBoardCommand.class)))
                .willReturn(out);
        mockMvc.perform(
                        get("/boards/{boardId}", 1L)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("성공적으로 불러왔습니다."))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("자유게시판"))
                .andExpect(jsonPath("$.data.slug").value("free"))
                .andExpect(jsonPath("$.data.allowedWriteRoles").value("USER"))
                .andExpect(jsonPath("$.data.posts").isArray());
        ArgumentCaptor<GetOneBoardCommand> captor = ArgumentCaptor.forClass(GetOneBoardCommand.class);

        verify(boardService).getOne(captor.capture());

        GetOneBoardCommand command = captor.getValue();

        assertThat(command.id()).isEqualTo(1L);
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시판_전체_조회")
    void getAllBoard() throws Exception {
        GetBoardListResponse item = new GetBoardListResponse(
                1L,
                "자유게시판",
                "free"
        );
        PageInfo pageInfo = new PageInfo(
                0,
                20,
                1L,
                1
        );
        OffsetPageResponse<GetBoardListResponse> response =
                new OffsetPageResponse<>(List.of(item), pageInfo);

        given(boardService.getAll(any(GetBoardListQuery.class)))
                .willReturn(response);
        mockMvc.perform(
                        get("/boards")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("댓글 목록을 성공적으로 조회했습니다."))
                .andExpect(jsonPath("$.data.content[0].boardId").value(1L))
                .andExpect(jsonPath("$.data.content[0].name").value("자유게시판"))
                .andExpect(jsonPath("$.data.content[0].slug").value("free"))
                .andExpect(jsonPath("$.data.pageInfo.page").value(0))
                .andExpect(jsonPath("$.data.pageInfo.size").value(20))
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(1L))
                .andExpect(jsonPath("$.data.pageInfo.totalPages").value(1));
        ArgumentCaptor<GetBoardListQuery> captor = ArgumentCaptor.forClass(GetBoardListQuery.class);

        verify(boardService).getAll(captor.capture());

        GetBoardListQuery query = captor.getValue();

        assertThat(query.page()).isEqualTo(0);
        assertThat(query.size()).isEqualTo(10);
        assertThat(query.direction()).isEqualTo(Sort.Direction.DESC);

        verifyNoMoreInteractions(boardService);
    }
}