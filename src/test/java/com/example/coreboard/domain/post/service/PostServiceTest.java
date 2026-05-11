package com.example.coreboard.domain.post.service;

import com.example.coreboard.domain.attachment.entity.Attachment;
import com.example.coreboard.domain.attachment.entity.AttachmentStatus;
import com.example.coreboard.domain.attachment.repository.AttachmentRepository;
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
    AttachmentRepository attachmentRepository;

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
        verify(attachmentService).confirm(boardCreateCommand.attachmentIds(), saved, admin);
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

        Users user = new Users(
                "username1",
                "nickname",
                "password1",
                "qwe1@qwe.com",
                "010-1234-1231",
                UserRole.USER
        );
        ReflectionTestUtils.setField(user, "userId", 5L);

        Post post = new Post(
                board,
                user,
                "title1",
                "content1",
                ContentFormat.MARKDOWN
        );
        ReflectionTestUtils.setField(post, "id", id);

        SliceResponse<GetAllCommentResponse> emptyComments = new SliceResponse<>(
                List.of(),
                new SliceInfo(10, 0, false)
        );

        given(postRepository.findByIdAndStatus(id, PostStatus.PUBLISHED))
                .willReturn(Optional.of(post));

        given(commentService.getAll(any(GetCommentQuery.class)))
                .willReturn(emptyComments);

        given(attachmentRepository.findByPostIdAndStatus(id, AttachmentStatus.CONFIRMED))
                .willReturn(List.of());

        GetOnePostResult out = postService.getOne(new GetOnePostCommand(id));

        assertNotNull(out);
        assertEquals(id, out.id());
        assertEquals(5L, out.userId());
        assertEquals("title1", out.title());
        assertEquals("content1", out.content());

        verify(postRepository).findByIdAndStatus(id, PostStatus.PUBLISHED);
        verify(commentService).getAll(any(GetCommentQuery.class));
        verify(attachmentRepository).findByPostIdAndStatus(id, AttachmentStatus.CONFIRMED);
        verifyNoMoreInteractions(postRepository);
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
    @DisplayName("게시글_단건_조회_첨부파일이_있으면_첨부파일_응답으로_매핑")
    void findOneWithAttachments() {
        Long id = 1L;

        Board board = freeBoard();

        Users user = new Users(
                "username1",
                "nickname",
                "password1",
                "qwe1@qwe.com",
                "010-1234-1231",
                UserRole.USER
        );
        ReflectionTestUtils.setField(user, "userId", 5L);

        Post post = new Post(
                board,
                user,
                "title1",
                "content1",
                ContentFormat.MARKDOWN
        );
        ReflectionTestUtils.setField(post, "id", id);

        Attachment attachment = Attachment.createTemp(
                user,
                "cat.png",
                "attachments/temp/cat.png",
                "http://localhost:9000/coreboard-attachments/attachments/temp/cat.png",
                "image/png",
                5L
        );
        ReflectionTestUtils.setField(attachment, "id", 100L);

        SliceResponse<GetAllCommentResponse> emptyComments = new SliceResponse<>(
                List.of(),
                new SliceInfo(10, 0, false)
        );

        given(postRepository.findByIdAndStatus(id, PostStatus.PUBLISHED))
                .willReturn(Optional.of(post));

        given(commentService.getAll(any(GetCommentQuery.class)))
                .willReturn(emptyComments);

        given(attachmentRepository.findByPostIdAndStatus(id, AttachmentStatus.CONFIRMED))
                .willReturn(List.of(attachment));

        GetOnePostResult out = postService.getOne(new GetOnePostCommand(id));

        assertNotNull(out);
        assertEquals(1, out.attachments().size());
        assertEquals(100L, out.attachments().get(0).id());
        assertEquals("cat.png", out.attachments().get(0).originalFileName());
        assertEquals("http://localhost:9000/coreboard-attachments/attachments/temp/cat.png", out.attachments().get(0).storeUrl());
        assertEquals("image/png", out.attachments().get(0).contentType());
        assertEquals(5L, out.attachments().get(0).fileSize());

        verify(postRepository).findByIdAndStatus(id, PostStatus.PUBLISHED);
        verify(commentService).getAll(any(GetCommentQuery.class));
        verify(attachmentRepository).findByPostIdAndStatus(id, AttachmentStatus.CONFIRMED);
    }

    @Test
    @DisplayName("게시글_수정_성공")
    void update() {
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
                "기존제목",
                "기존본문",
                ContentFormat.MARKDOWN
        );
        ReflectionTestUtils.setField(post, "id", id);

        UpdatePostCommand cmd = new UpdatePostCommand(
                id,
                username,
                "새제목",
                "새본문",
                ContentFormat.MARKDOWN,
                List.of(),
                List.of()
        );

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(loginUser));
        given(postRepository.findByIdAndStatus(id, PostStatus.PUBLISHED)).willReturn(Optional.of(post));
        given(attachmentRepository.findByPostIdAndStatus(id, AttachmentStatus.CONFIRMED))
                .willReturn(List.of());

        UpdatePostResult result = postService.update(cmd);

        assertNotNull(result);
        assertEquals(id, result.id());

        assertEquals("새제목", post.getTitle());
        assertEquals("새본문", post.getContent());
        assertEquals(ContentFormat.MARKDOWN, post.getContentFormat());

        verify(usersRepository).findByUsername(username);
        verify(postRepository).findByIdAndStatus(id, PostStatus.PUBLISHED);
        verify(attachmentRepository).findByPostIdAndStatus(id, AttachmentStatus.CONFIRMED);

        verify(attachmentService).updatePostAttachments(
                post,
                loginUser,
                List.of(),
                List.of()
        );
    }

    @Test
    @DisplayName("게시글_수정_권한없음_403")
    void updateFobiddern() {
        Users loginUser = mock(Users.class);
        Post post = mock(Post.class);

        UpdatePostCommand cmd = new UpdatePostCommand(
                1L,
                "tester",
                "title",
                "content",
                ContentFormat.MARKDOWN,
                List.of(),
                List.of()
        );

        given(usersRepository.findByUsername("tester")).willReturn(Optional.of(loginUser));
        given(postRepository.findByIdAndStatus(1L, PostStatus.PUBLISHED)).willReturn(Optional.of(post));
        given(post.isWrittenBy(loginUser)).willReturn(false);
        given(loginUser.getRole()).willReturn(UserRole.USER);

        AuthErrorException exception = assertThrows(
                AuthErrorException.class,
                () -> postService.update(cmd)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());

        verify(post, never()).update(anyString(), anyString(), any(ContentFormat.class));
        verify(attachmentRepository, never()).findByPostIdAndStatus(anyLong(), any());
        verify(attachmentService, never()).updatePostAttachments(any(), any(), any(), any());
    }

    @Test
    @DisplayName("게시글_수정_ADMIN은_작성자가_아니어도_가능")
    void updateByAdmin() {
        Long id = 1L;

        Users admin = mock(Users.class);
        Post post = mock(Post.class);
        Board board = freeBoard();

        UpdatePostCommand cmd = new UpdatePostCommand(
                id,
                "admin",
                "관리자수정제목",
                "관리자수정본문",
                ContentFormat.MARKDOWN,
                List.of(),
                List.of()
        );

        given(usersRepository.findByUsername("admin")).willReturn(Optional.of(admin));
        given(postRepository.findByIdAndStatus(id, PostStatus.PUBLISHED)).willReturn(Optional.of(post));
        given(post.isWrittenBy(admin)).willReturn(false);
        given(admin.getRole()).willReturn(UserRole.ADMIN);
        given(post.getId()).willReturn(id);
        given(post.getBoard()).willReturn(board);
        given(attachmentRepository.findByPostIdAndStatus(id, AttachmentStatus.CONFIRMED)).willReturn(List.of());

        UpdatePostResult result = postService.update(cmd);

        assertNotNull(result);
        assertEquals(id, result.id());

        verify(post).update("관리자수정제목", "관리자수정본문", ContentFormat.MARKDOWN);
        verify(attachmentService).updatePostAttachments(
                post,
                admin,
                List.of(),
                List.of()
        );
    }

    @Test
    @DisplayName("게시글_수정_keepAttachmentIds와_newAttachmentIds가_null_이면_기존_첨부파일을_유지하고_새첨부파일은_없음으로_처리")
    void updateAttachmentIdsNull() {
        Long id = 1L;
        String username = "tester";

        Users loginUser = new Users(
                username,
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.USER
        );
        ReflectionTestUtils.setField(loginUser, "userId", 10L);

        Board board = freeBoard();
        ReflectionTestUtils.setField(board, "requireAttachment", false);
        ReflectionTestUtils.setField(board, "maxAttachmentCount", 2);

        Post post = new Post(
                board,
                loginUser,
                "기존제목",
                "기존본문",
                ContentFormat.MARKDOWN
        );
        ReflectionTestUtils.setField(post, "id", id);

        Attachment currentAttachment = mock(Attachment.class);

        UpdatePostCommand cmd = new UpdatePostCommand(
                id,
                username,
                "새제목",
                "새본문",
                null,
                null,
                null
        );

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(loginUser));
        given(postRepository.findByIdAndStatus(id, PostStatus.PUBLISHED)).willReturn(Optional.of(post));
        given(attachmentRepository.findByPostIdAndStatus(id, AttachmentStatus.CONFIRMED))
                .willReturn(List.of(currentAttachment));

        UpdatePostResult result = postService.update(cmd);

        assertNotNull(result);
        assertEquals(id, result.id());

        assertEquals("새제목", post.getTitle());
        assertEquals("새본문", post.getContent());

        // 핵심: contentFormat은 null로 수정 요청했으므로 기존 MARKDOWN 유지
        assertEquals(ContentFormat.MARKDOWN, post.getContentFormat());

        verify(usersRepository).findByUsername(username);
        verify(postRepository).findByIdAndStatus(id, PostStatus.PUBLISHED);
        verify(attachmentRepository).findByPostIdAndStatus(id, AttachmentStatus.CONFIRMED);

        verify(attachmentService).updatePostAttachments(
                post,
                loginUser,
                null,
                null
        );
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

        OffsetPageResponse<PostSummaryResponse> result = postService.getAll(1L, 0, 10, "desc", null);

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
        verify(postRepository, never()).searchByBoardId(anyLong(), any(), anyString(), any());
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

        OffsetPageResponse<PostSummaryResponse> result = postService.getAll(1L, 0, 10, "asc", null);

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
        verify(postRepository, never()).searchByBoardId(anyLong(), any(), anyString(), any());
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    @DisplayName("게시글_전체조회_keyword가_있으면_검색_쿼리_사용")
    void getAllWithKeyword() {
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
                "spring title",
                "spring content",
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

        given(postRepository.searchByBoardId(
                1L,
                PostStatus.PUBLISHED,
                "spring",
                pageRequest
        )).willReturn(postPage);

        OffsetPageResponse<PostSummaryResponse> result = postService.getAll(1L, 0, 10, "desc", "spring");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).id());
        assertEquals("nickname", result.getContent().get(0).writerName());
        assertEquals("spring title", result.getContent().get(0).title());

        assertEquals(0, result.getPageInfo().getPage());
        assertEquals(10, result.getPageInfo().getSize());
        assertEquals(1L, result.getPageInfo().getTotalElements());
        assertEquals(1, result.getPageInfo().getTotalPages());

        verify(postRepository).searchByBoardId(
                1L,
                PostStatus.PUBLISHED,
                "spring",
                pageRequest
        );
        verify(postRepository, never()).findAllByBoardId(anyLong(), any(), any());
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    @DisplayName("게시글_전체조회_keyword가_공백이면_일반_목록을_조회")
    void getAllWithBlankKeyword() {
        Board board = freeBoard();

        Users user = new Users(
                "username",
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.USER
        );

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

        OffsetPageResponse<PostSummaryResponse> result = postService.getAll(1L, 0, 10, "desc", "   ");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(postRepository).findAllByBoardId(
                1L,
                PostStatus.PUBLISHED,
                pageRequest
        );
        verify(postRepository, never()).searchByBoardId(anyLong(), any(), anyString(), any());
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    @DisplayName("게시글_전체조회_keyword는_앞뒤_공백을_제거하고_검색")
    void getAllWithKeywordTrim() {
        Board board = freeBoard();

        Users user = new Users(
                "username",
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.USER
        );

        Post post = new Post(
                board,
                user,
                "spring title",
                "spring content",
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

        given(postRepository.searchByBoardId(
                1L,
                PostStatus.PUBLISHED,
                "spring",
                pageRequest
        )).willReturn(postPage);

        OffsetPageResponse<PostSummaryResponse> result = postService.getAll(1L, 0, 10, "desc", "  spring  ");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(postRepository).searchByBoardId(
                1L,
                PostStatus.PUBLISHED,
                "spring",
                pageRequest
        );
        verify(postRepository, never()).findAllByBoardId(anyLong(), any(), any());
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
        verify(attachmentService).markDeletedByPost(id);
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("게시글_삭제_권한없으면_첨부파일_삭제상태_처리하지_않는다")
    void deleteForbiddenDoesNotMarkAttachmentsDeleted() {
        Users loginUser = mock(Users.class);
        Post post = mock(Post.class);

        String username = "tester";
        DeletePostCommand command = new DeletePostCommand(1L, username);

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(loginUser));
        given(postRepository.findByIdAndStatus(1L, PostStatus.PUBLISHED)).willReturn(Optional.of(post));
        given(post.isWrittenBy(loginUser)).willReturn(false);

        AuthErrorException exception = assertThrows(
                AuthErrorException.class,
                () -> postService.delete(command)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());

        verify(usersRepository).findByUsername(username);
        verify(postRepository).findByIdAndStatus(1L, PostStatus.PUBLISHED);
        verify(post, never()).delete();
        verify(attachmentService, never()).markDeletedByPost(anyLong());
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("게시글_삭제_게시글이_없으면_첨부파일_삭제상태_처리하지_않는다")
    void deletePostNotFoundDoesNotMarkAttachmentsDeleted() {
        String username = "tester";
        Long postId = 1L;

        Users loginUser = mock(Users.class);
        DeletePostCommand command = new DeletePostCommand(postId, username);

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(loginUser));
        given(postRepository.findByIdAndStatus(postId, PostStatus.PUBLISHED)).willReturn(Optional.empty());

        PostErrorException exception = assertThrows(
                PostErrorException.class,
                () -> postService.delete(command)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

        verify(usersRepository).findByUsername(username);
        verify(postRepository).findByIdAndStatus(postId, PostStatus.PUBLISHED);
        verify(attachmentService, never()).markDeletedByPost(anyLong());
        verify(postRepository, never()).delete(any(Post.class));
    }
}