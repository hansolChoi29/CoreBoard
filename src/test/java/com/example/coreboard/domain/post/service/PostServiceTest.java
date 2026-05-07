package com.example.coreboard.domain.post.service;

import com.example.coreboard.domain.attachment.service.AttachmentService;
import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.comment.dto.query.GetCommentQuery;
import com.example.coreboard.domain.comment.dto.response.GetAllCommentResponse;
import com.example.coreboard.domain.comment.service.CommentService;
import com.example.coreboard.domain.common.response.OffsetPageResponse;
import com.example.coreboard.domain.common.response.SliceInfo;
import com.example.coreboard.domain.common.response.SliceResponse;
import com.example.coreboard.domain.post.dto.command.CreatePostCommand;
import com.example.coreboard.domain.post.dto.command.DeletePostCommand;
import com.example.coreboard.domain.post.dto.command.GetOnePostCommand;
import com.example.coreboard.domain.post.dto.command.UpdatePostCommand;
import com.example.coreboard.domain.post.dto.request.CreatePostRequest;
import com.example.coreboard.domain.post.dto.response.PostSummaryResponse;
import com.example.coreboard.domain.post.dto.result.CreatePostResult;
import com.example.coreboard.domain.post.dto.result.GetOnePostResult;
import com.example.coreboard.domain.post.dto.result.UpdatePostResult;
import com.example.coreboard.domain.post.entity.ContentFormat;
import com.example.coreboard.domain.post.entity.Post;
import com.example.coreboard.domain.post.entity.PostStatus;
import com.example.coreboard.domain.post.repository.PostRepository;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.post.PostErrorException;
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
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import static com.example.coreboard.domain.support.fixture.BoardFixture.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock
    CommentService commentService;

    @Mock
    BoardRepository boardRepository;

    @Mock
    PostRepository postRepository;

    @Mock
    UsersRepository usersRepository;

    @Mock
    AttachmentService attachmentService;

    @InjectMocks
    PostService postService;

    CreatePostRequest boardCreateRequest;
    CreatePostCommand boardCreateCommand;

    @BeforeEach
    void setUpCreate() {
        boardCreateRequest = new CreatePostRequest(
                "title",
                "content",
                ContentFormat.MARKDOWN,
                List.of()
        );

        boardCreateCommand = new CreatePostCommand(
                1L,
                "제목",
                "내용",
                ContentFormat.MARKDOWN,
                List.of()
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

        CreatePostResult result = postService.create(boardCreateCommand, "tester");

        assertNotNull(result);
        assertEquals(1L, result.id());

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());

        Post toSave = captor.getValue();

        assertEquals(10L, toSave.getUser().getUserId());
        assertEquals("제목", toSave.getTitle());
        assertEquals("내용", toSave.getContent());

        verify(attachmentService).confirm(boardCreateCommand.attachmentIds(), saved, users);
        verify(usersRepository).findByUsername("tester");
        verify(postRepository).existsByTitle("제목");
        verify(boardRepository).findById(1L);
    }

    @Test
    @DisplayName("게시글_생성_ADMIN은_ADMIN전용_게시판에_작성할_수_있다")
    void createAdminBoardByAdmin() {
        String username = "admin";

        Users admin = new Users(
                username,
                "nickname",
                "password",
                "admin@test.com",
                "01012341234",
                UserRole.ADMIN
        );
        ReflectionTestUtils.setField(admin, "userId", 10L);

        Board board = noticeBoard();

        Post saved = new Post(
                board,
                admin,
                "제목",
                "내용",
                ContentFormat.MARKDOWN
        );
        ReflectionTestUtils.setField(saved, "id", 1L);

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(admin));
        given(postRepository.existsByTitle("제목")).willReturn(false);
        given(boardRepository.findById(1L)).willReturn(Optional.of(board));
        given(postRepository.save(any(Post.class))).willReturn(saved);

        CreatePostResult result = postService.create(boardCreateCommand, username);

        assertNotNull(result);
        assertEquals(1L, result.id());

        verify(usersRepository).findByUsername(username);
        verify(postRepository).existsByTitle("제목");
        verify(boardRepository).findById(1L);
        verify(postRepository).save(any(Post.class));
        verify(attachmentService).confirm(boardCreateCommand.attachmentIds(), saved, admin);
    }

    @Test
    @DisplayName("게시글_생성_ADMIN은_USER허용_게시판에도_작성할_수_있다")
    void createUserBoardByAdmin() {
        String username = "admin";

        Users admin = new Users(
                username,
                "nickname",
                "password",
                "admin@test.com",
                "01012341234",
                UserRole.ADMIN
        );
        ReflectionTestUtils.setField(admin, "userId", 10L);

        Board board = freeBoard();

        Post saved = new Post(
                board,
                admin,
                "제목",
                "내용",
                ContentFormat.MARKDOWN
        );
        ReflectionTestUtils.setField(saved, "id", 1L);

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(admin));
        given(postRepository.existsByTitle("제목")).willReturn(false);
        given(boardRepository.findById(1L)).willReturn(Optional.of(board));
        given(postRepository.save(any(Post.class))).willReturn(saved);

        CreatePostResult result = postService.create(boardCreateCommand, username);

        assertNotNull(result);
        assertEquals(1L, result.id());

        verify(usersRepository).findByUsername(username);
        verify(postRepository).existsByTitle("제목");
        verify(boardRepository).findById(1L);
        verify(postRepository).save(any(Post.class));
        verify(attachmentService).confirm(boardCreateCommand.attachmentIds(), saved,admin);
    }

    @Test
    @DisplayName("게시글_생성_USER는_USER허용_게시판에_작성할_수_있다")
    void createUserBoardByUser() {
        String username = "tester";

        Users users = new Users(
                username,
                "nickname",
                "password",
                "user01@naver.com",
                "01012341234",
                UserRole.USER
        );
        ReflectionTestUtils.setField(users, "userId", 10L);

        Board board = freeBoard();

        Post saved = new Post(
                board,
                users,
                "제목",
                "내용",
                ContentFormat.MARKDOWN
        );
        ReflectionTestUtils.setField(saved, "id", 1L);

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(users));
        given(postRepository.existsByTitle("제목")).willReturn(false);
        given(boardRepository.findById(1L)).willReturn(Optional.of(board));
        given(postRepository.save(any(Post.class))).willReturn(saved);

        CreatePostResult result = postService.create(boardCreateCommand, username);

        assertNotNull(result);
        assertEquals(1L, result.id());

        verify(usersRepository).findByUsername(username);
        verify(postRepository).existsByTitle("제목");
        verify(boardRepository).findById(1L);
        verify(postRepository).save(any(Post.class));
        verify(attachmentService).confirm(boardCreateCommand.attachmentIds(), saved, users);
    }

    @Test
    @DisplayName("게시글_생성_예외_유저없음_404")
    void createUserNotFound() {
        given(usersRepository.findByUsername("tester")).willReturn(Optional.empty());

        AuthErrorException notFoundUser = assertThrows(
                AuthErrorException.class,
                () -> postService.create(boardCreateCommand, "tester"));

        assertEquals(HttpStatus.NOT_FOUND, notFoundUser.getStatus());
        verify(attachmentService, never()).confirm(any(), any(Post.class), any(Users.class));
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
        verify(attachmentService, never()).confirm(any(), any(Post.class), any(Users.class));
        verify(usersRepository).findByUsername("tester");
        verify(postRepository).existsByTitle("제목");
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("게시글_생성_첨부파일_비허용_게시판에_첨부파일_요청시_400")
    void createAttachmentNotAllowed() {
        Users users = new Users(
                "tester",
                "nickname",
                "password",
                "user01@naver.com",
                "01012341234",
                UserRole.USER
        );
        ReflectionTestUtils.setField(users, "userId", 10L);

        Board board = new Board(
                "자유게시판",
                "free",
                false,
                false,
                false,
                0,
                UserRole.USER
        );

        CreatePostCommand command = new CreatePostCommand(
                1L,
                "제목",
                "내용",
                ContentFormat.MARKDOWN,
                List.of(1L)
        );

        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(users));
        given(postRepository.existsByTitle("제목")).willReturn(false);
        given(boardRepository.findById(1L)).willReturn(Optional.of(board));

        PostErrorException exception = assertThrows(
                PostErrorException.class,
                () -> postService.create(command, "tester")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(attachmentService, never()).confirm(any(), any(Post.class), any(Users.class));
        verify(usersRepository).findByUsername("tester");
        verify(postRepository).existsByTitle("제목");
        verify(boardRepository).findById(1L);
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("게시글_생성_첨부파일_필수_게시판에_첨부파일_없으면_400")
    void createAttachmentRequiredButEmpty() {
        Users users = new Users(
                "tester",
                "nickname",
                "password",
                "user01@naver.com",
                "01012341234",
                UserRole.USER
        );
        ReflectionTestUtils.setField(users, "userId", 10L);

        Board board = freeBoard();
        ReflectionTestUtils.setField(board, "requireAttachment", true);
        ReflectionTestUtils.setField(board, "maxAttachmentCount", 2);

        CreatePostCommand command = new CreatePostCommand(
                1L,
                "제목",
                "내용",
                ContentFormat.MARKDOWN,
                List.of()
        );

        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(users));
        given(postRepository.existsByTitle("제목")).willReturn(false);
        given(boardRepository.findById(1L)).willReturn(Optional.of(board));

        PostErrorException exception = assertThrows(
                PostErrorException.class,
                () -> postService.create(command, "tester")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(attachmentService, never()).confirm(any(), any(Post.class), any(Users.class));
        verify(usersRepository).findByUsername("tester");
        verify(postRepository).existsByTitle("제목");
        verify(boardRepository).findById(1L);
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("게시글_생성_첨부파일_허용개수_이내면_성공")
    void createAttachmentAllowed() {
        Users users = new Users(
                "tester",
                "nickname",
                "password",
                "user01@naver.com",
                "01012341234",
                UserRole.USER
        );
        ReflectionTestUtils.setField(users, "userId", 10L);

        Board board = new Board(
                "자료게시판",
                "archive",
                false,
                false,
                false,
                0,
                UserRole.USER
        );
        ReflectionTestUtils.setField(board, "requireAttachment", false);
        ReflectionTestUtils.setField(board, "maxAttachmentCount", 2);

        CreatePostCommand command = new CreatePostCommand(
                1L,
                "제목",
                "내용",
                ContentFormat.MARKDOWN,
                List.of(1L, 2L)
        );

        Post saved = new Post(
                board,
                users,
                "제목",
                "내용",
                ContentFormat.MARKDOWN
        );
        ReflectionTestUtils.setField(saved, "id", 1L);

        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(users));
        given(postRepository.existsByTitle("제목")).willReturn(false);
        given(boardRepository.findById(1L)).willReturn(Optional.of(board));
        given(postRepository.save(any(Post.class))).willReturn(saved);

        CreatePostResult result = postService.create(command, "tester");

        assertNotNull(result);
        assertEquals(1L, result.id());

        verify(usersRepository).findByUsername("tester");
        verify(postRepository).existsByTitle("제목");
        verify(boardRepository).findById(1L);
        verify(postRepository).save(any(Post.class));
        verify(attachmentService).confirm(command.attachmentIds(), saved, users);
    }

    @Test
    @DisplayName("게시글_생성_첨부파일_최대개수_초과시_400")
    void createAttachmentCountExceeded() {
        Users users = new Users(
                "tester",
                "nickname",
                "password",
                "user01@naver.com",
                "01012341234",
                UserRole.USER
        );
        ReflectionTestUtils.setField(users, "userId", 10L);

        Board board = new Board(
                "자료게시판",
                "archive",
                true,
                false,
                false,
                2,
                UserRole.USER
        );

        CreatePostCommand command = new CreatePostCommand(
                1L,
                "제목",
                "내용",
                ContentFormat.MARKDOWN,
                List.of(1L, 2L, 3L)
        );

        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(users));
        given(postRepository.existsByTitle("제목")).willReturn(false);
        given(boardRepository.findById(1L)).willReturn(Optional.of(board));

        PostErrorException exception = assertThrows(
                PostErrorException.class,
                () -> postService.create(command, "tester")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(attachmentService, never()).confirm(any(), any(Post.class), any(Users.class));
        verify(usersRepository).findByUsername("tester");
        verify(postRepository).existsByTitle("제목");
        verify(boardRepository).findById(1L);
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("게시글_생성_contentFormat이_null이면_MARKDOWN으로_생성")
    void createContentFormatNull() {
        Users users = new Users(
                "tester",
                "nickname",
                "password",
                "user01@naver.com",
                "01012341234",
                UserRole.USER
        );
        ReflectionTestUtils.setField(users, "userId", 10L);

        Board board = freeBoard();

        CreatePostCommand command = new CreatePostCommand(
                1L,
                "제목",
                "내용",
                null,
                List.of()
        );

        Post saved = new Post(
                board,
                users,
                "제목",
                "내용",
                ContentFormat.MARKDOWN
        );
        ReflectionTestUtils.setField(saved, "id", 1L);

        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(users));
        given(postRepository.existsByTitle("제목")).willReturn(false);
        given(boardRepository.findById(1L)).willReturn(Optional.of(board));
        given(postRepository.save(any(Post.class))).willReturn(saved);

        CreatePostResult result = postService.create(command, "tester");

        assertNotNull(result);
        assertEquals(1L, result.id());

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());

        Post toSave = captor.getValue();

        assertEquals(ContentFormat.MARKDOWN, toSave.getContentFormat());
    }

    @Test
    @DisplayName("게시글_생성_attachmentIds가_null이면_첨부파일_0개로_처리")
    void createAttachmentIdsNull() {
        Users users = new Users(
                "tester",
                "nickname",
                "password",
                "user01@naver.com",
                "01012341234",
                UserRole.USER
        );
        ReflectionTestUtils.setField(users, "userId", 10L);

        Board board = freeBoard();
        ReflectionTestUtils.setField(board, "requireAttachment", false);
        ReflectionTestUtils.setField(board, "maxAttachmentCount", 0);

        CreatePostCommand command = new CreatePostCommand(
                1L,
                "제목",
                "내용",
                ContentFormat.MARKDOWN,
                null
        );

        Post saved = new Post(
                board,
                users,
                "제목",
                "내용",
                ContentFormat.MARKDOWN
        );
        ReflectionTestUtils.setField(saved, "id", 1L);

        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(users));
        given(postRepository.existsByTitle("제목")).willReturn(false);
        given(boardRepository.findById(1L)).willReturn(Optional.of(board));
        given(postRepository.save(any(Post.class))).willReturn(saved);

        CreatePostResult result = postService.create(command, "tester");

        assertNotNull(result);
        assertEquals(1L, result.id());

        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글_생성_첨부파일_필수_게시판에_첨부파일이_있으면_성공")
    void createAttachmentRequiredAndExists() {
        Users users = new Users(
                "tester",
                "nickname",
                "password",
                "user01@naver.com",
                "01012341234",
                UserRole.USER
        );
        ReflectionTestUtils.setField(users, "userId", 10L);

        Board board = freeBoard();
        ReflectionTestUtils.setField(board, "requireAttachment", true);
        ReflectionTestUtils.setField(board, "maxAttachmentCount", 2);

        CreatePostCommand command = new CreatePostCommand(
                1L,
                "제목",
                "내용",
                ContentFormat.MARKDOWN,
                List.of(1L)
        );

        Post saved = new Post(
                board,
                users,
                "제목",
                "내용",
                ContentFormat.MARKDOWN
        );
        ReflectionTestUtils.setField(saved, "id", 1L);

        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(users));
        given(postRepository.existsByTitle("제목")).willReturn(false);
        given(boardRepository.findById(1L)).willReturn(Optional.of(board));
        given(postRepository.save(any(Post.class))).willReturn(saved);

        CreatePostResult result = postService.create(command, "tester");

        assertNotNull(result);
        assertEquals(1L, result.id());

        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글_생성_게시판_작성권한없음_403")
    void createFordibbenByBoardWriteRole() {
        String username = "username";
        Long id = 1L;

        Users user = new Users(
                username,
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.USER
        );
        ReflectionTestUtils.setField(user, "userId", 10L);

        Board board = noticeBoard();

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(postRepository.existsByTitle("제목")).willReturn(false);
        given(boardRepository.findById(id)).willReturn(Optional.of(board));

        assertThrows(
                AuthErrorException.class,
                () -> postService.create(boardCreateCommand, username)
        );

        verify(usersRepository).findByUsername(username);
        verify(postRepository).existsByTitle("제목");
        verify(boardRepository).findById(id);
        verify(postRepository, never()).save(any(Post.class));
    }



    @Test
    @DisplayName("게시글_단건_조회_성공")
    void findOne() {
        Long id = 1L;
        Board board = freeBoard();
        Users user = new Users("username1", "nickname", "password1", "qwe1@qwe.com", "010-1234-1231", UserRole.USER);
        ReflectionTestUtils.setField(user, "userId", 5L);

        SliceResponse<GetAllCommentResponse> emptyComments = new SliceResponse<>(
                List.of(),
                new SliceInfo(10, 0, false)
        );

        Post post = new Post(
                board,
                user,
                "title1", "content1", ContentFormat.MARKDOWN);
        ReflectionTestUtils.setField(post, "id", id);

        given(postRepository.findByIdAndStatus(id, PostStatus.PUBLISHED)).willReturn(Optional.of(post));

        GetOnePostResult out = postService.getOne(new GetOnePostCommand(id));

        assertNotNull(out);
        assertEquals(id, out.id());
        verify(postRepository, times(1)).findByIdAndStatus(id, PostStatus.PUBLISHED);
        verifyNoMoreInteractions(postRepository);
        verify(commentService).getAll(any(GetCommentQuery.class));
    }

    @Test
    @DisplayName("게시글_단건_조회_미존재_404")
    void findOnNotFound() {
        Long id = 1L;
        given(postRepository.findByIdAndStatus(id, PostStatus.PUBLISHED)).willReturn(Optional.empty());
        GetOnePostCommand command = new GetOnePostCommand(id);

        PostErrorException findOneNotFound = assertThrows(PostErrorException.class,
                () -> postService.getOne(command));

        assertEquals(HttpStatus.NOT_FOUND, findOneNotFound.getStatus());
        verify(postRepository, times(1)).findByIdAndStatus(id, PostStatus.PUBLISHED);
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    @DisplayName("게시글_수정_성공")
    void update() {
        Long id = 1L;

        Users loginUser = mock(Users.class);
        Post post = mock(Post.class);

        UpdatePostCommand cmd = new UpdatePostCommand(
                id,
                "tester",
                "새제목",
                "새본문",
                ContentFormat.MARKDOWN
        );

        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(loginUser));
        given(postRepository.findByIdAndStatus(id, PostStatus.PUBLISHED)).willReturn(Optional.of(post));
        given(post.isWrittenBy(loginUser)).willReturn(true);
        given(post.getId()).willReturn(id);

        UpdatePostResult result = postService.update(cmd);

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

        UpdatePostCommand cmd = new UpdatePostCommand(
                1L,
                "tester",
                "title",
                "content",
                ContentFormat.MARKDOWN
        );

        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(loginUser));
        given(postRepository.findByIdAndStatus(1L, PostStatus.PUBLISHED)).willReturn(Optional.of(post));
        given(post.isWrittenBy(loginUser)).willReturn(false);

        AuthErrorException exception = assertThrows(
                AuthErrorException.class,
                () -> postService.update(cmd)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());

        verify(post, never()).update(anyString(), anyString(), any(ContentFormat.class));
    }

    @Test
    @DisplayName("게시글_전체조회_오프셋_desc_성공")
    void getAllOffsetDesc() {
        Board board = freeBoard();

        Users user = new Users(
                "username",
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.USER
        );
        ReflectionTestUtils.setField(user, "userId", 1L);

        Post post = new Post(
                board,
                user,
                "title",
                "content",
                ContentFormat.MARKDOWN
        );
        ReflectionTestUtils.setField(post, "id", 1L);

        PageRequest pageRequest = PageRequest.of(
                0,
                10,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Post> postPage = new PageImpl<>(
                List.of(post),
                pageRequest,
                1
        );

        given(postRepository.findAllByBoardId(
                1L,
                PostStatus.PUBLISHED,
                pageRequest
        )).willReturn(postPage);

        OffsetPageResponse<PostSummaryResponse> result =
                postService.getAll(1L, 0, 10, "desc");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).id());
        assertEquals("nickname", result.getContent().get(0).writerName());
        assertEquals("title", result.getContent().get(0).title());

        assertEquals(0, result.getPageInfo().getPage());
        assertEquals(10, result.getPageInfo().getSize());
        assertEquals(1L, result.getPageInfo().getTotalElements());
        assertEquals(1, result.getPageInfo().getTotalPages());

        verify(postRepository).findAllByBoardId(
                1L,
                PostStatus.PUBLISHED,
                pageRequest
        );
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    @DisplayName("게시글_전체조회_오프셋_asc_성공")
    void getAllOffsetAsc() {
        Board board = freeBoard();

        Users user = new Users(
                "username",
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.USER
        );
        ReflectionTestUtils.setField(user, "userId", 1L);

        Post post = new Post(
                board,
                user,
                "title",
                "content",
                ContentFormat.MARKDOWN
        );
        ReflectionTestUtils.setField(post, "id", 1L);

        PageRequest pageRequest = PageRequest.of(
                0,
                10,
                Sort.by(Sort.Direction.ASC, "createdAt")
        );

        Page<Post> postPage = new PageImpl<>(
                List.of(post),
                pageRequest,
                1
        );

        given(postRepository.findAllByBoardId(
                1L,
                PostStatus.PUBLISHED,
                pageRequest
        )).willReturn(postPage);

        OffsetPageResponse<PostSummaryResponse> result =
                postService.getAll(1L, 0, 10, "asc");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("title", result.getContent().get(0).title());

        assertEquals(0, result.getPageInfo().getPage());
        assertEquals(10, result.getPageInfo().getSize());
        assertEquals(1L, result.getPageInfo().getTotalElements());
        assertEquals(1, result.getPageInfo().getTotalPages());

        verify(postRepository).findAllByBoardId(
                1L,
                PostStatus.PUBLISHED,
                pageRequest
        );
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    @DisplayName("게시글_삭제_성공")
    void delete() {
        Long id = 1L;
        String username = "tester";

        Board board = freeBoard();

        Users loginUser = new Users(
                username,
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.USER
        );
        ReflectionTestUtils.setField(loginUser, "userId", 10L);

        Post post = new Post(
                board,
                loginUser,
                "제목",
                "내용",
                ContentFormat.MARKDOWN
        );
        ReflectionTestUtils.setField(post, "id", id);

        DeletePostCommand command = new DeletePostCommand(id, username);

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(loginUser));
        given(postRepository.findByIdAndStatus(id, PostStatus.PUBLISHED)).willReturn(Optional.of(post));

        postService.delete(command);

        assertEquals(PostStatus.DELETED, post.getStatus());

        verify(usersRepository).findByUsername(username);
        verify(postRepository).findByIdAndStatus(id, PostStatus.PUBLISHED);
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("게시글_삭제_권한없음_403")
    void deleteFobiddern() {
        Users loginUser = mock(Users.class);
        Post post = mock(Post.class);

        String username = "tester";
        DeletePostCommand command = new DeletePostCommand(1L, username);

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(loginUser));
        given(postRepository.findByIdAndStatus(1L, PostStatus.PUBLISHED)).willReturn(Optional.of(post));

        AuthErrorException exception = assertThrows(
                AuthErrorException.class,
                () -> postService.delete(command)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());

        verify(usersRepository).findByUsername(username);
        verify(postRepository).findByIdAndStatus(1L, PostStatus.PUBLISHED);

        verify(post, never()).delete();
        verify(postRepository, never()).delete(any(Post.class));
    }
}