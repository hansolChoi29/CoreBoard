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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import static org.mockito.Mockito.*;
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
        CreateBoardRequest request = new CreateBoardRequest(
                "자유게시판",
                "free",
                false,
                false,
                false,
                0,
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
        assertThat(command.answerAcceptedEnabled()).isFalse();
        assertThat(command.commentEnabled()).isFalse();
        assertThat(command.allowedWriteRoles()).isEqualTo(UserRole.USER);

        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시글생성_name_isBlank")
    void createValidateName() throws Exception {
        String username = "username";
        CreateBoardRequest request = new CreateBoardRequest(
                "",
                "free",
                false,
                false,
                false,
                0,
                UserRole.USER
        );
        mockMvc.perform(
                        post("/admin/boards")
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("게시판 이름은 필수입니다."));
        verifyNoInteractions(boardService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "aaaaaaaaaaaaaaaaaaaaa"})
    @DisplayName("게시글생성_name_length_invalid")
    void createValidatevNameLengthInvalid(String name) throws Exception {
        String username = "username";
        CreateBoardRequest request = new CreateBoardRequest(
                name,
                "free",
                false,
                false,
                false,
                0,
                UserRole.USER
        );
        mockMvc.perform(
                        post("/admin/boards")
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("게시판 이름은 2자 이상 20자 이하로 입력해 주세요."));
        verifyNoMoreInteractions(boardService);
    }

    @ParameterizedTest
    @CsvSource({
            "'', '게시판 주소는 필수입니다.'",
            "a, '게시판 주소는 2자 이상 50자 이하로 입력해 주세요.'",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa, '게시판 주소는 2자 이상 50자 이하로 입력해 주세요.'",
            "Free, '게시판 주소는 영문 소문자, 숫자, 하이픈만 사용할 수 있습니다.'",
            "free_board, '게시판 주소는 영문 소문자, 숫자, 하이픈만 사용할 수 있습니다.'"
    })
    @DisplayName("게시판생성_slug_length_invalid")
    void createValidateSlug(String slug, String expectedMessage) throws Exception {
        String username = "username";
        CreateBoardRequest request = new CreateBoardRequest(
                "자유게시판",
                slug,
                false,
                false,
                false,
                0,
                UserRole.USER
        );
        mockMvc.perform(
                        post("/admin/boards")
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(expectedMessage));
        verifyNoInteractions(boardService);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 3})
    @DisplayName("게시판생성_maxAttachmentCount_invalid")
    void createValidateMaxAttachmentCountInvalid(int maxAttachmentCount) throws Exception {
        CreateBoardRequest request = new CreateBoardRequest(
                "자유게시판",
                "free",
                false,
                false,
                false,
                maxAttachmentCount,
                UserRole.USER
        );
        mockMvc.perform(
                        post("/admin/boards")
                                .requestAttr("username", "username")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("최대 첨부파일 개수는 0개 이상 2개 이하로 입력해 주세요."));
        verifyNoInteractions(boardService);
    }

    @Test
    @DisplayName("게시판_생성_첨부파일_필수인데_최대개수_0이면_400")
    void createBoardAttachmentRequiredButMaxCountZero() throws Exception {
        String username = "admin";
        String request = """
                {
                  "name": "자료게시판",
                  "slug": "archive",
                  "requireAttachment": true,
                  "commentEnabled": false,
                  "answerAcceptedEnabled": false,
                  "maxAttachmentCount": 0,
                  "allowedWriteRoles": "USER"
                }
                """;
        mockMvc.perform(
                        post("/admin/boards")
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("첨부파일을 필수로 설정하려면 최대 첨부파일 개수는 1개 이상이어야 합니다."));
        verify(boardService, never()).create(any(), anyString());
    }

    @Test
    @DisplayName("게시판_생성_첨부파일_필수이고_최대개수_1이면_201")
    void createBoardAttachmentRequiredAndMaxCountValid() throws Exception {
        String username = "admin";
        String request = """
                {
                  "name": "자료게시판",
                  "slug": "archive",
                  "requireAttachment": true,
                  "commentEnabled": false,
                  "answerAcceptedEnabled": false,
                  "maxAttachmentCount": 1,
                  "allowedWriteRoles": "USER"
                }
                """;
        CreateBoardResult result = new CreateBoardResult(1L);

        given(boardService.create(any(), eq(username))).willReturn(result);
        mockMvc.perform(
                        post("/admin/boards")
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));
        verify(boardService).create(any(), eq(username));
    }

    @Test
    @DisplayName("게시판생성_requiredWriteRole_null")
    void createValidateRequiredWriteRoleNull() throws Exception {
        CreateBoardRequest request = new CreateBoardRequest(
                "자유게시판",
                "free",
                false,
                false,
                false,
                0,
                null
        );
        mockMvc.perform(
                        post("/admin/boards")
                                .requestAttr("username", "username")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("게시글 작성 권한은 필수입니다."));
        verifyNoInteractions(boardService);
    }

    @ParameterizedTest
    @CsvSource({
            "false, 1",
            "true, 0"
    })
    @DisplayName("게시판생성_attachment_policy_invalid")
    void createValidateAttachmentPolicyInvalid(
            boolean requireAttachment,
            int maxAttachmentCount
    ) throws Exception {
        CreateBoardRequest request = new CreateBoardRequest(
                "자유게시판",
                "free",
                false,
                false,
                requireAttachment,
                maxAttachmentCount,
                UserRole.USER
        );

        mockMvc.perform(
                        post("/admin/boards")
                                .requestAttr("username", "username")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("첨부파일을 필수로 설정하려면 최대 첨부파일 개수는 1개 이상이어야 합니다."));

        verifyNoInteractions(boardService);
    }

    @Test
    @DisplayName("게시판생성_비로그인")
    void createUnauthOrized() throws Exception {
        CreateBoardRequest request = new CreateBoardRequest(
                "자유게시판",
                "free",
                false,
                false,
                false,
                0,
                UserRole.USER);
        mockMvcWithInterceptor.perform(
                        post("/admin/boards")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("다시 로그인해 주세요."));
        verify(boardService, never()).create(any(), anyString());
    }

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
                0);
        UpdateBoardResult result = new UpdateBoardResult(1L);

        given(boardService.update(any(UpdateBoardCommand.class), eq(username), eq(1L))).willReturn(result);
        mockMvc.perform(
                        patch("/admin/boards/{id}", 1L)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("성공적으로 수정되었습니다."))
                .andExpect(jsonPath("$.data.id").value(1L));
        ArgumentCaptor<UpdateBoardCommand> captor = ArgumentCaptor.forClass(UpdateBoardCommand.class);

        verify(boardService).update(captor.capture(), eq(username), eq(1L));

        UpdateBoardCommand command = captor.getValue();
        assertThat(command.id()).isEqualTo(1L);
        assertThat(command.name()).isEqualTo("자유게시판");
        assertThat(command.slug()).isEqualTo("free");
        assertThat(command.answerAcceptedEnabled()).isFalse();
        assertThat(command.commentEnabled()).isFalse();
        assertThat(command.requireAttachment()).isFalse();
        assertThat(command.maxAttachmentCount()).isEqualTo(0);
        verifyNoMoreInteractions(boardService);
    }

    @Test
    @DisplayName("게시판수정_비로그인")
    void updateUnauthOrized() throws Exception {
        Long id = 1L;
        UpdateBoardRequest request = new UpdateBoardRequest(id, "자유게시판", "free", false, false, false, 0);
        mockMvcWithInterceptor.perform(
                        patch("/admin/boards/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("다시 로그인해 주세요."));
        verify(boardService, never()).update(any(), anyString(), anyLong());
    }

    @Test
    @DisplayName("게시판_삭제_성공")
    void deleteBoard() throws Exception {
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

    @Test
    @DisplayName("게시판삭제_비로그인")
    void deleteUnauthOrized() throws Exception {
        Long id = 1L;
        mockMvcWithInterceptor.perform(
                        delete("/admin/boards/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("다시 로그인해 주세요."));
        verify(boardService, never()).delete(any());
    }
}