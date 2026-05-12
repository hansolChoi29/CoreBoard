package com.example.coreboard.domain.post.controller;

import com.example.coreboard.domain.common.exception.auth.AuthErrorCode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.response.OffsetPageResponse;
import com.example.coreboard.domain.common.response.PageInfo;
import com.example.coreboard.domain.post.dto.request.CreatePostRequest;
import com.example.coreboard.domain.post.dto.response.PostSummaryResponse;
import com.example.coreboard.domain.post.dto.result.CreatePostResult;
import com.example.coreboard.domain.common.type.ContentFormat;
import com.example.coreboard.domain.post.service.PostService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
class BoardPostControllerTest {
    private static final String BASE = "/boards/{boardId}/posts";
    ObjectMapper objectMapper = new ObjectMapper();
    String username = "tester";
    long id = 1;
    long boardId = 1L;

    @Mock
    PostService postService;

    @InjectMocks
    BoardPostController boardPostController;

    MockMvc mockMvc;
    MockMvc mockMvcWithInterceptor;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcSupport.create(boardPostController);
        mockMvcWithInterceptor = MockMvcSupport.createWithInterceptor(boardPostController);
    }

    @Test
    @DisplayName("게시글_생성")
    void create() throws Exception {
        String username = "tester";
        CreatePostResult dummy = new CreatePostResult(id);
        given(postService.create(any(), eq(username))).willReturn(dummy);

        CreatePostRequest request = new CreatePostRequest("title", "content", ContentFormat.MARKDOWN, List.of());
        mockMvc.perform(
                        post(BASE, boardId)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("게시글이 성공적으로 생성되었습니다."))
                .andExpect(jsonPath("$.data.id").value(1));
        verify(postService).create(any(), eq(username));
    }

    @Test
    @DisplayName("게시글_생성_유저없음_404")
    void createIsNotUser() throws Exception {
        CreatePostRequest request = new CreatePostRequest(
                "title", "content", ContentFormat.MARKDOWN, List.of());

        given(postService.create(any(), eq("ghost")))
                .willThrow(new AuthErrorException(AuthErrorCode.NOT_FOUND));
        mockMvc.perform(
                        post(BASE, boardId)
                                .requestAttr("username", "ghost")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));
        verify(postService).create(any(), eq("ghost"));
        verifyNoMoreInteractions(postService);
    }

    @Test
    @DisplayName("게시글_생성_로그인_안함_401")
    void createUnauthorized() throws Exception {
        CreatePostRequest request = new CreatePostRequest("titile", "content", ContentFormat.MARKDOWN, List.of());
        mockMvcWithInterceptor.perform(
                        post(BASE, boardId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("다시 로그인해 주세요."));
        verify(postService, never()).create(any(), anyString());
    }

    @Test
    @DisplayName("게시글_생성_제목과_본문_비어있음_400")
    void creatteTitleAndContentIsBlank() throws Exception {
        CreatePostRequest request = new CreatePostRequest("", "", ContentFormat.MARKDOWN, List.of());
        mockMvc.perform(
                        post(BASE, boardId)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목과 내용은 필수입니다."));
        verify(postService, never()).create(any(), anyString());
    }

    @Test
    @DisplayName("게시글_생성_제목_400")
    void createContentIsBlank() throws Exception {
        CreatePostRequest request = new CreatePostRequest("title", "", ContentFormat.MARKDOWN, List.of());
        mockMvc.perform(
                        post(BASE, boardId)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("본문은 필수입니다."));
        verify(postService, never()).create(any(), anyString());
    }

    @Test
    @DisplayName("게시글_생성_제목_400")
    void createTitleOrContentIsBlank() throws Exception {
        CreatePostRequest request = new CreatePostRequest("", "content", ContentFormat.MARKDOWN, List.of());
        mockMvc.perform(
                        post(BASE, boardId)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목은 필수입니다."));
        verify(postService, never()).create(any(), anyString());
    }

    @Test
    @DisplayName("게시글_생성_제목_너무_김_400")
    void createTitleToLong() throws Exception {
        String longTitle = "a".repeat(256);
        CreatePostRequest request = new CreatePostRequest(longTitle, "zx", ContentFormat.MARKDOWN, List.of());
        mockMvc.perform(
                        post(BASE, boardId)
                                .requestAttr("username", username)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목은 255자 이하여야 합니다."));
        verify(postService, never()).create(any(), anyString());
    }

    @Test
    @DisplayName("게시글_생성_본문_너무_김_400")
    void createContentToLong() throws Exception {
        String longContent = "a".repeat(100100);
        CreatePostRequest request = new CreatePostRequest("title", longContent, ContentFormat.MARKDOWN, List.of());
        mockMvc.perform(
                        post(BASE, boardId)
                                .requestAttr("username", "tester")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("본문은 10000자 이하여야 합니다."));
        verify(postService, never()).create(any(), anyString());
    }

    @Test
    @DisplayName("게시글_전체_조회")
    void getAll() throws Exception {
        List<PostSummaryResponse> items = List.of(
                new PostSummaryResponse(
                        1L,
                        "nickname",
                        "title",
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )
        );

        PageInfo pageInfo = new PageInfo(
                0,
                10,
                1L,
                1
        );

        OffsetPageResponse<PostSummaryResponse> offsetResponse =
                new OffsetPageResponse<>(items, pageInfo);

        given(postService.getAll(boardId, 0, 10, "desc", null))
                .willReturn(offsetResponse);

        mockMvc.perform(
                        get(BASE, boardId)
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "desc")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글 전체 조회!"))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].writerName").value("nickname"))
                .andExpect(jsonPath("$.data.content[0].title").value("title"))
                .andExpect(jsonPath("$.data.pageInfo.page").value(0))
                .andExpect(jsonPath("$.data.pageInfo.size").value(10))
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(1))
                .andExpect(jsonPath("$.data.pageInfo.totalPages").value(1));

        verify(postService).getAll(boardId, 0, 10, "desc", null);
        verifyNoMoreInteractions(postService);
    }

    @Test
    @DisplayName("게시글_전체조회_쿼리파라미터_없으면_기본값으로_조회")
    void getAllDefaultParams() throws Exception {
        List<PostSummaryResponse> items = List.of(
                new PostSummaryResponse(
                        1L,
                        "nickname",
                        "title",
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )
        );

        PageInfo pageInfo = new PageInfo(0, 10, 1L, 1);
        OffsetPageResponse<PostSummaryResponse> offsetResponse =
                new OffsetPageResponse<>(items, pageInfo);

        given(postService.getAll(boardId, 0, 10, "desc", null))
                .willReturn(offsetResponse);

        mockMvc.perform(
                        get(BASE, boardId)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글 전체 조회!"))
                .andExpect(jsonPath("$.data.content[0].id").value(1));

        verify(postService).getAll(boardId, 0, 10, "desc", null);
        verifyNoMoreInteractions(postService);
    }

    @Test
    @DisplayName("게시글_전체조회_keyword가_있으면_검색어를_서비스로_전달")
    void getAllWithKeyword() throws Exception {
        String keyword = "spring";

        List<PostSummaryResponse> items = List.of(
                new PostSummaryResponse(
                        1L,
                        "nickname",
                        "spring title",
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )
        );

        PageInfo pageInfo = new PageInfo(0, 10, 1L, 1);
        OffsetPageResponse<PostSummaryResponse> offsetResponse =
                new OffsetPageResponse<>(items, pageInfo);

        given(postService.getAll(boardId, 0, 10, "desc", keyword))
                .willReturn(offsetResponse);

        mockMvc.perform(
                        get(BASE, boardId)
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "desc")
                                .param("keyword", keyword)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글 전체 조회!"))
                .andExpect(jsonPath("$.data.content[0].title").value("spring title"));

        verify(postService).getAll(boardId, 0, 10, "desc", keyword);
        verifyNoMoreInteractions(postService);
    }

    @Test
    @DisplayName("게시글_전체_조회_Size_10_이상_400")
    void getAllSizeTooLonger() throws Exception {
        mockMvc.perform(
                        get(BASE, boardId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("size", "11")
                                .param("sort", "desc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("size는 1 이상 10 이하이어야 합니다."));
        verify(postService, never()).getAll(
                anyLong(),
                anyInt(),
                anyInt(),
                anyString(),
                any()
        );
    }

    @Test
    @DisplayName("게시글_전체_조회_정렬_방향_공백_400")
    void getAllBlankSortDirection() throws Exception {
        mockMvc.perform(
                        get(BASE, boardId)
                                .param("size", "10")
                                .param("sort", " ")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("정렬 방향은 asc 또는 desc만 허용됩니다."));
        verify(postService, never()).getAll(
                anyLong(),
                anyInt(),
                anyInt(),
                anyString(),
                any()
        );
        verifyNoMoreInteractions(postService);
    }

    @Test
    @DisplayName("게시글_전체조회_size_0")
    void getAllShortSize() throws Exception {
        mockMvc.perform(
                        get(BASE, boardId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("size", "0")
                                .param("sort", "asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("size는 1 이상 10 이하이어야 합니다."));
        verify(postService, never()).getAll(
                anyLong(),
                anyInt(),
                anyInt(),
                anyString(),
                any()
        );
    }

    @Test
    @DisplayName("게시글_전체조회_size_11")
    void getAllTooLongsize() throws Exception {
        mockMvc.perform(
                        get(BASE, boardId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("size", "11")
                                .param("sort", "desc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("size는 1 이상 10 이하이어야 합니다."));
        verify(postService, never()).getAll(
                anyLong(),
                anyInt(),
                anyInt(),
                anyString(),
                any()
        );
    }

    @Test
    @DisplayName("게시글_전체_조회_desc_정상")
    void getAllDesc() throws Exception {
        String keyword = "spring";
        List<PostSummaryResponse> items = List.of(
                new PostSummaryResponse(
                        10L,
                        "nickname",
                        "title",
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )
        );

        PageInfo pageInfo = new PageInfo(
                0,
                10,
                1L,
                1
        );

        OffsetPageResponse<PostSummaryResponse> offsetResponse =
                new OffsetPageResponse<>(items, pageInfo);

        given(postService.getAll(boardId, 0, 10, "desc", null)).willReturn(offsetResponse);

        mockMvc.perform(
                        get(BASE, boardId)
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "desc")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글 전체 조회!"));

        verify(postService).getAll(boardId, 0, 10, "desc", null);
    }

    @Test
    @DisplayName("게시글_전체_조회_정렬_방향_잘못됨_404")
    void getAllInvalidSortDirection() throws Exception {
        mockMvc.perform(
                        get(BASE, boardId)
                                .param("size", "10")
                                .param("sort", "wrong")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("정렬 방향은 asc 또는 desc만 허용됩니다."));
        verify(postService, never()).getAll(
                anyLong(),
                anyInt(),
                anyInt(),
                anyString(),
                any()
        );
        verifyNoMoreInteractions(postService);
    }
}