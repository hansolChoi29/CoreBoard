package com.example.coreboard.domain.post.service;

import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.post.dto.*;
import com.example.coreboard.domain.post.dto.command.PostCreateCommand;
import com.example.coreboard.domain.post.dto.command.PostGetOneCommand;
import com.example.coreboard.domain.post.dto.command.PostUpdateCommand;
import com.example.coreboard.domain.post.dto.request.PostCreateRequest;
import com.example.coreboard.domain.post.dto.response.PostSummaryKeysetResponse;
import com.example.coreboard.domain.post.entity.ContentFormat;
import com.example.coreboard.domain.post.entity.Post;
import com.example.coreboard.domain.post.repository.PostRepository;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.post.PostErrorException;
import com.example.coreboard.domain.common.response.CursorResponse;
import com.example.coreboard.domain.users.entity.UserRole;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import static com.example.coreboard.domain.support.fixture.BoardFixture.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock
    BoardRepository boardRepository;

    @Mock
    PostRepository postRepository;

    @Mock
    UsersRepository usersRepository;

    @InjectMocks
    PostService postService;

    PostCreateRequest boardCreateRequest;
    PostCreateCommand boardCreateCommand;

    @BeforeEach
    void setUpCreate() {
        boardCreateRequest = new PostCreateRequest(
                1L,
                "title",
                "content",
                ContentFormat.MARKDOWN
        );

        boardCreateCommand = new PostCreateCommand(
                1L,
                "제목",
                "내용",
                ContentFormat.MARKDOWN
        );
    }

    @Test
    @DisplayName("게시글_생성")
    void create() {
        Users users = new Users("tester", "nickname", "password", "user01@naver.com", "01012341234", UserRole.USER);
        ReflectionTestUtils.setField(users, "userId", 10L);

        Board board = freeBoard();

        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(users));
        given(postRepository.existsByTitle("제목")).willReturn(false);
        given(boardRepository.findById(1L)).willReturn(Optional.of(board));

        Post saved = new Post(board, users, "제목", "내용", ContentFormat.MARKDOWN);
        ReflectionTestUtils.setField(saved, "id", 1L);

        given(postRepository.save(any(Post.class))).willReturn(saved);

        PostCreateDto result = postService.create(boardCreateCommand, "tester");

        assertNotNull(result);
        assertEquals(1L, result.id());

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());

        Post toSave = captor.getValue();

        assertEquals(10L, toSave.getUser().getUserId());
        assertEquals("제목", toSave.getTitle());
        assertEquals("내용", toSave.getContent());

        verify(usersRepository).findByUsername("tester");
        verify(postRepository).existsByTitle("제목");
        verify(boardRepository).findById(1L);
    }

    @Test
    @DisplayName("게시글_생성_예외_유저없음_404")
    void createUserNotFound() {
        given(usersRepository.findByUsername("tester")).willReturn(Optional.empty());

        AuthErrorException notFoundUser = assertThrows(
                AuthErrorException.class,
                () -> postService.create(boardCreateCommand, "tester"));

        assertEquals(HttpStatus.NOT_FOUND, notFoundUser.getStatus());

        verify(postRepository, never()).existsByTitle(anyString());
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("게시글_생성_중복_제목_409")
    void createTitleDuplicated() {
        Users users = mock(Users.class);
        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(users));
        given(postRepository.existsByTitle("제목")).willReturn(true);

        PostErrorException duplicatedBoard = assertThrows(
                PostErrorException.class,
                () -> postService.create(boardCreateCommand, "tester"));

        assertEquals(HttpStatus.CONFLICT, duplicatedBoard.getStatus());

        verify(usersRepository).findByUsername("tester");
        verify(postRepository).existsByTitle("제목");
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("게시글_단건_조회_성공")
    void findOne() {
        Long id = 1L;
        Board board = freeBoard();
        Users user = new Users("username1", "nickname", "password1", "qwe1@qwe.com", "010-1234-1231", UserRole.USER);
        ReflectionTestUtils.setField(user, "userId", 5L);  // userId null 방지

        Post post = new Post(
                board,
                user,
                "title1", "content1", ContentFormat.MARKDOWN);
        ReflectionTestUtils.setField(post, "id", id);

        given(postRepository.findById(id)).willReturn(Optional.of(post));

        PostGetOneDto out = postService.findOne(new PostGetOneCommand(id));

        assertNotNull(out);
        assertEquals(id, out.id());
        verify(postRepository, times(1)).findById(id);
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    @DisplayName("게시글_단건_조회_미존재_404")
    void findOnNotFound() {
        Long id = 1L;
        given(postRepository.findById(id)).willReturn(Optional.empty());
        PostGetOneCommand command = new PostGetOneCommand(id);

        PostErrorException findOneNotFound = assertThrows(PostErrorException.class,
                () -> postService.findOne(command));

        assertEquals(HttpStatus.NOT_FOUND, findOneNotFound.getStatus());
        verify(postRepository, times(1)).findById(id);
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    @DisplayName("게시글_전체_조회_첫페이지_커서_없음_hasNext_false")
    void findAll_firstPage_noNextPage() {
        Board board = freeBoard();
        List<Post> boards = List.of(
                new Post(
                        board,
                        new Users("username1", "nickname", "password1", "qwe1@qwe.com", "010-1234-1231", UserRole.USER),
                        "title1", "content1", ContentFormat.MARKDOWN),
                new Post(
                        board,
                        new Users("username1", "nickname", "password1", "qwe1@qwe.com", "010-1234-1231", UserRole.USER),
                        "title2", "content2", ContentFormat.MARKDOWN),
                new Post(
                        board,
                        new Users("username1", "nickname", "password1", "qwe1@qwe.com", "010-1234-1231", UserRole.USER),
                        "title3", "content3", ContentFormat.MARKDOWN));

        Pageable pageable = PageRequest.of(0, 11);
        given(postRepository.findFirstPageDesc(pageable)).willReturn(boards);
        CursorResponse<PostSummaryKeysetResponse> result = postService.findAll(null, null, 10, "desc");

        assertEquals(3, result.getContents().size());
        assertFalse(result.isHasNext());
        assertNull(result.getNextCursorTitle());
        assertNull(result.getNextCursorId());

        verify(postRepository, times(1)).findFirstPageDesc(pageable);
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    @DisplayName("게시글_전체_조회_첫페이지_커서_없음_hasNext_true_커서세팅")
    void findAll_firstPage_hasNextPage() {
        Board board = freeBoard();

        Users user = new Users("username", "nickname", "password", "qwe@qwe.com", "01012341234", UserRole.USER);
        ReflectionTestUtils.setField(user, "userId", 1L);

        List<Post> posts = new ArrayList<>();

        for (long i = 11; i >= 1; i--) {
            Post post = new Post(board, user, "title" + i, "content" + i, ContentFormat.MARKDOWN);
            ReflectionTestUtils.setField(post, "id", i);
            posts.add(post);
        }

        Pageable pageable = PageRequest.of(0, 11);
        given(postRepository.findFirstPageDesc(pageable)).willReturn(posts);

        CursorResponse<PostSummaryKeysetResponse> result =
                postService.findAll(null, null, 10, "desc");

        assertEquals(10, result.getContents().size());
        assertTrue(result.isHasNext());
        assertEquals("title2", result.getNextCursorTitle());
        assertEquals(2L, result.getNextCursorId());

        verify(postRepository, times(1)).findFirstPageDesc(pageable);
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    @DisplayName("게시글_전체_조회_다음_페이지에서_asc_분기")
    void findAll_nextPage_asc_branch_cover() {
        Board board = freeBoard();
        List<Post> boards = new ArrayList<>();
        for (long i = 11; i >= 1; i--) {
            boards.add(new Post(
                    board ,
                    new Users("username1", "nickname", "password1", "qwe1@qwe.com", "010-1234-1231", UserRole.USER), "title" + i, "content" + i, ContentFormat.MARKDOWN));
        }

        given(postRepository.findNextPageAsc(eq("title2"), eq(12L), any(Pageable.class)))
                .willReturn(boards);

        CursorResponse<PostSummaryKeysetResponse> result =
                postService.findAll("title2", 12L, 10, "asc");

        assertEquals(10, result.getContents().size());
        assertTrue(result.isHasNext());

        verify(postRepository, times(1))
                .findNextPageAsc(eq("title2"), eq(12L), any(Pageable.class));
        verify(postRepository, never())
                .findNextPageDesc(anyString(), anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("게시글_전체_조회_다음페이지_커서_있음_hasNext_false")
    void findAll_nextPage_noNextPage() {
        Board board = freeBoard();
        List<Post> boards = List.of(
                new Post(
                        board,
                        new Users("username1", "nickname", "password1", "qwe1@qwe.com", "010-1234-1231", UserRole.USER),
                        "title1", "content1", ContentFormat.MARKDOWN),
                new Post(
                        board,
                        new Users("username2", "nickname", "password2", "qwe2@qwe.com", "010-1234-1232", UserRole.USER),
                        "title2", "content2", ContentFormat.MARKDOWN));
        Pageable pageable = PageRequest.of(0, 11);
        given(postRepository.findNextPageDesc("title", 5L, pageable)).willReturn(boards);
        CursorResponse<PostSummaryKeysetResponse> result = postService.findAll("title", 5L, 10, "desc");
        assertEquals(2, result.getContents().size());
        assertFalse(result.isHasNext());
        assertNull(result.getNextCursorTitle());
        assertNull(result.getNextCursorId());

        verify(postRepository, times(1)).findNextPageDesc("title", 5L, pageable);
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    @DisplayName("게시글_전체_조회_다음페이지_커서_있음_hasNext_true_커서세팅")
    void findAll_nextPage_hasNextPage() {
        Board board = freeBoard();

        Users user = new Users("username", "nickname", "password", "qwe@qwe.com", "01012341234", UserRole.USER);
        ReflectionTestUtils.setField(user, "userId", 1L);

        List<Post> posts = new ArrayList<>();

        for (long i = 11; i >= 1; i--) {
            Post post = new Post(board, user, "title" + i, "content" + i, ContentFormat.MARKDOWN);
            ReflectionTestUtils.setField(post, "id", i);
            posts.add(post);
        }

        Pageable pageable = PageRequest.of(0, 11);
        given(postRepository.findNextPageDesc("title2", 12L, pageable)).willReturn(posts);

        CursorResponse<PostSummaryKeysetResponse> result =
                postService.findAll("title2", 12L, 10, "desc");

        assertEquals(10, result.getContents().size());
        assertTrue(result.isHasNext());
        assertEquals("title2", result.getNextCursorTitle());
        assertEquals(2L, result.getNextCursorId());

        verify(postRepository).findNextPageDesc("title2", 12L, pageable);
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    @DisplayName("커서가_null이면_findFirstPage_호출")
    void findAll_title_or_id_null() {
        Board board = freeBoard();
        Users user = new Users("username", "nickname", "password", "qwe@qwe.com", "01012341234", UserRole.USER);

        List<Post> mockData = createBoards(5, board, user);
        Pageable pageable = PageRequest.of(0, 11);
        given(postRepository.findFirstPageDesc(pageable)).willReturn(mockData);

        postService.findAll(null, null, 10, "desc");

        verify(postRepository).findFirstPageDesc(pageable);
        verify(postRepository, never()).findNextPageDesc(anyString(), anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("결과가_size보다_많으면_hasNext_true고_size개만_반환")
    void findAll_size_hasNext_true_size_return() {
        Board board = freeBoard();
        Users user = new Users("username", "nickname", "password", "qwe@qwe.com", "01012341234", UserRole.USER);

        List<Post> mockData = createBoards(5, board, user);

        Pageable pageable = PageRequest.of(0, 11);
        given(postRepository.findNextPageDesc("title", 1L, pageable)).willReturn(mockData);

        postService.findAll("title", 1L, 10, "desc");

        verify(postRepository).findNextPageDesc("title", 1L, pageable);
        verify(postRepository, never()).findFirstPageDesc(pageable);
    }

    @Test
    @DisplayName("결과가_size_이하면_hasNext_false")
    void findAll_size_hasNext_false() {
        Board board = freeBoard();
        Users user = new Users("username", "nickname", "password", "qwe@qew.com", "01012341234", UserRole.USER);

        Pageable pageable = PageRequest.of(0, 11);

        given(postRepository.findFirstPageDesc(pageable))
                .willReturn(createBoards(5, board, user));

        CursorResponse<PostSummaryKeysetResponse> result =
                postService.findAll(null, null, 10, "desc");

        assertEquals(5, result.getContents().size());
        assertFalse(result.isHasNext());
        assertNull(result.getNextCursorTitle());
        assertNull(result.getNextCursorId());

        verify(postRepository).findFirstPageDesc(pageable);
        verify(postRepository, never()).findNextPageDesc(anyString(), anyLong(), any(Pageable.class));
        verifyNoMoreInteractions(postRepository);
    }

    private List<Post> createBoards(int count, Board board, Users user) {
        return IntStream.range(0, count)
                .mapToObj(i -> Post.create(
                        board,
                        user,
                        "title" + i,
                        "content" + i
                ))
                .collect(Collectors.toList());
    }

    @Test
    @DisplayName("게시글_수정_성공")
    void update() {
        Long id = 1L;

        Users loginUser = mock(Users.class);
        Post post = mock(Post.class);
        Users postWriter = mock(Users.class);

        PostUpdateCommand cmd = new PostUpdateCommand(
                id,
                "tester",
                "새제목",
                "새본문",
                ContentFormat.MARKDOWN
        );

        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(loginUser));
        given(postRepository.findById(id)).willReturn(Optional.of(post));

        given(loginUser.getUserId()).willReturn(10L);
        given(post.getUser()).willReturn(postWriter);
        given(postWriter.getUserId()).willReturn(10L);

        given(post.getId()).willReturn(id);

        PostUpdatedDto result = postService.update(cmd);

        assertNotNull(result);
        assertEquals(id, result.id());

        verify(post).update("새제목", "새본문", ContentFormat.MARKDOWN);
    }

    @Test
    @DisplayName("게시글_수정_권한없음_403")
    void updateFobiddern() {
        Users loginUser = mock(Users.class);
        Post post = mock(Post.class);
        Users postWriter = mock(Users.class);

        PostUpdateCommand cmd = new PostUpdateCommand(
                1L,
                "tester",
                "title",
                "content",
                ContentFormat.MARKDOWN
        );

        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(loginUser));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        given(loginUser.getUserId()).willReturn(10L);
        given(post.getUser()).willReturn(postWriter);
        given(postWriter.getUserId()).willReturn(98L);

        AuthErrorException exception = assertThrows(
                AuthErrorException.class,
                () -> postService.update(cmd)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());

        verify(post, never()).update(anyString(), anyString(), any(ContentFormat.class));
    }

    @Test
    @DisplayName("게시글_삭제_성공")
    void delete() {
        Users loginUser = mock(Users.class);
        Post post = mock(Post.class);
        Users postWriter = mock(Users.class);

        String username = "tester";
        Long id = 1L;

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(loginUser));
        given(postRepository.findById(id)).willReturn(Optional.of(post));

        given(loginUser.getUserId()).willReturn(10L);
        given(post.getUser()).willReturn(postWriter);
        given(postWriter.getUserId()).willReturn(10L);

        postService.delete(username, id);

        verify(usersRepository, times(1)).findByUsername(username);
        verify(postRepository, times(1)).findById(id);
        verify(postRepository, times(1)).delete(post);
    }

    @Test
    @DisplayName("게시글_삭제_권한없음_403")
    void deleteFobiddern() {
        Users loginUser = mock(Users.class);
        Post post = mock(Post.class);
        Users postWriter = mock(Users.class);

        String username = "tester";

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(loginUser));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        given(loginUser.getUserId()).willReturn(10L);
        given(post.getUser()).willReturn(postWriter);
        given(postWriter.getUserId()).willReturn(98L);

        AuthErrorException exception = assertThrows(
                AuthErrorException.class,
                () -> postService.delete(username, 1L)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());

        verify(usersRepository, times(1)).findByUsername(username);
        verify(postRepository, times(1)).findById(1L);
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("게시글_검색_성공")
    void search() {
        Board boards = freeBoard();
        String keyword = "key";

        Post post = new Post(
                boards,
                new Users("username1", "nickname", "password1", "qwe1@qwe.com", "010-1234-1231", UserRole.USER),
                "title1", "content", ContentFormat.MARKDOWN
        );
        Post post2 = new Post(
                boards,
                new Users("username2", "nickname", "password2", "qwe2@qwe.com", "010-1234-1232", UserRole.USER),
                "title2", "content", ContentFormat.MARKDOWN
        );
        when(postRepository.searchByKeyword(keyword))
                .thenReturn(List.of(post, post2));

        CursorResponse<PostSummaryKeysetResponse> result = postService.search(keyword);

        assertEquals(2, result.getContents().size());
        assertTrue(result.getContents().stream()
                .anyMatch(board -> board.title().equals("title1")));
        assertTrue(result.getContents().stream()
                .anyMatch(board -> board.title().equals("title2")));
    }
    // TODO : 검색 페이지네이션 기준 정하기
    // TODO : 검색 Repository 쿼리 구체화하기
    // TODO : 검색 입력값 검증 추가하기
    // TODO : 검색 결과 없음, 공백 keyword, 대소문자/부분일치, 정렬, 페이지 분할 같은 케이스 추가
}