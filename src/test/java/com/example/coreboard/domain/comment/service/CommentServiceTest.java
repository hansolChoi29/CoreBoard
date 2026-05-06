package com.example.coreboard.domain.comment.service;

import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.comment.dto.command.CommentCommand;
import com.example.coreboard.domain.comment.dto.query.GetCommentQuery;
import com.example.coreboard.domain.comment.dto.response.GetAllCommentResponse;
import com.example.coreboard.domain.comment.dto.result.CommentResult;
import com.example.coreboard.domain.comment.entity.Comment;
import com.example.coreboard.domain.comment.entity.CommentStatus;
import com.example.coreboard.domain.comment.repository.CommentRepository;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.comment.CommentErrorException;
import com.example.coreboard.domain.common.exception.post.PostErrorException;
import com.example.coreboard.domain.common.response.SliceResponse;
import com.example.coreboard.domain.post.entity.ContentFormat;
import com.example.coreboard.domain.post.entity.Post;
import com.example.coreboard.domain.post.repository.PostRepository;
import com.example.coreboard.domain.users.entity.UserRole;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    String username = "username";
    @Mock
    CommentRepository commentRepository;
    @Mock
    PostRepository postRepository;
    @Mock
    UsersRepository usersRepository;
    @InjectMocks
    CommentService commentService;

    @Test
    @DisplayName("댓글_생성_실패_댓글_허용_안됨")
    void create_comment_not_allowed() {
        Long postId = 1L;
        Long userId = 100L;

        Users user = createUser(userId, username, UserRole.USER);

        Board board = Board.create(
                "자유게시판",
                "free",
                false,
                false,
                false,
                0,
                UserRole.USER
        );
        ReflectionTestUtils.setField(board, "id", 1L);

        Post post = createPost(postId, board, user);

        CommentCommand command = new CommentCommand("content");

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> commentService.create(postId, username, command))
                .isInstanceOf(CommentErrorException.class);
    }

    @Test
    @DisplayName("댓글_생성_성공")
    void create() {
        Long postId = 1L;
        Long commentId = 10L;
        Long userId = 100L;

        String content = "content";

        Users user = createUser(userId, username, UserRole.USER);
        Board board = createBoard(1L);
        Post post = createPost(postId, board, user);

        Comment savedComment = Comment.create(
                post,
                user,
                content
        );
        ReflectionTestUtils.setField(savedComment, "id", commentId);

        CommentCommand command = new CommentCommand(content);

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

        CommentResult result = commentService.create(
                postId,
                username,
                command
        );

        assertThat(result.id()).isEqualTo(commentId);

        verify(usersRepository).findByUsername(username);
        verify(postRepository).findById(postId);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글_생성_실패_게시글_없음")
    void create_post_not_found() {
        Long postId = 1L;
        Long userId = 100L;

        Users user = createUser(userId, username, UserRole.USER);
        CommentCommand command = new CommentCommand("content");

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create(postId, username, command))
                .isInstanceOf(PostErrorException.class);
    }

    @Test
    @DisplayName("댓글_생성_실패_삭제된_게시글")
    void create_deleted_post() {
        Long postId = 1L;
        Long userId = 100L;

        Users user = createUser(userId, username, UserRole.USER);
        Board board = createBoard(1L);
        Post post = createPost(postId, board, user);
        post.delete();

        CommentCommand command = new CommentCommand("content");

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> commentService.create(postId, username, command))
                .isInstanceOf(PostErrorException.class);
    }

    @Test
    @DisplayName("댓글_전체조회_성공")
    void getAll() {
        Long postId = 1L;
        Long userId = 100L;
        Long commentId = 10L;

        GetCommentQuery query = new GetCommentQuery(postId, 0, 10);

        Users user = createUser(userId, username, UserRole.USER);
        Board board = createBoard(1L);
        Post post = createPost(postId, board, user);
        Comment comment = createComment(commentId, post, user, "content");

        List<Comment> commentList = List.of(comment);

        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by(
                        Sort.Order.desc("createdDate"),
                        Sort.Order.desc("id")
                )
        );

        Slice<Comment> mockSlice = new SliceImpl<>(
                commentList,
                pageable,
                false
        );

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.findByPostIdAndStatus(
                anyLong(),
                eq(CommentStatus.ACTIVE),
                any(Pageable.class)
        )).willReturn(mockSlice);

        SliceResponse<GetAllCommentResponse> response = commentService.getAll(query);

        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).commentId()).isEqualTo(commentId);
        assertThat(response.content().get(0).content()).isEqualTo("content");
        assertThat(response.content().get(0).nickname()).isEqualTo("nickname");

        verify(commentRepository).findByPostIdAndStatus(
                anyLong(),
                eq(CommentStatus.ACTIVE),
                any(Pageable.class)
        );
    }

    @Test
    @DisplayName("댓글_전체조회_실패_삭제된_게시글")
    void getAll_deleted_post() {
        Long postId = 1L;
        Long userId = 100L;

        GetCommentQuery query = new GetCommentQuery(postId, 0, 10);

        Users user = createUser(userId, username, UserRole.USER);
        Board board = createBoard(1L);
        Post post = createPost(postId, board, user);
        post.delete();

        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> commentService.getAll(query))
                .isInstanceOf(PostErrorException.class);
        verify(postRepository).findById(postId);
    }

    @Test
    @DisplayName("댓글_전체조회_실패_게시글_없음")
    void getAll_post_not_found() {
        Long postId = 1L;
        GetCommentQuery query = new GetCommentQuery(postId, 0, 10);

        given(postRepository.findById(postId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getAll(query))
                .isInstanceOf(PostErrorException.class);
        verify(postRepository).findById(postId);
    }

    @Test
    @DisplayName("댓글수정_성공")
    void update() {
        Long postId = 1L;
        Long commentId = 10L;
        Long userId = 100L;

        String beforeContent = "content";
        String afterContent = "updated content";

        Users user = createUser(userId, username, UserRole.USER);
        Board board = createBoard(1L);
        Post post = createPost(postId, board, user);
        Comment comment = createComment(commentId, post, user, beforeContent);

        CommentCommand command = new CommentCommand(afterContent);

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        CommentResult result = commentService.update(
                username,
                postId,
                commentId,
                command
        );

        assertThat(result.id()).isEqualTo(commentId);
        assertThat(comment.getContent()).isEqualTo(afterContent);

        verify(usersRepository).findByUsername(username);
        verify(postRepository).findById(postId);
        verify(commentRepository).findById(commentId);
    }

    @Test
    @DisplayName("댓글수정_실패_댓글_없음")
    void update_comment_not_found() {
        Long postId = 1L;
        Long commentId = 10L;
        Long userId = 100L;

        Users user = createUser(userId, username, UserRole.USER);
        Board board = createBoard(1L);
        Post post = createPost(postId, board, user);

        CommentCommand command = new CommentCommand("updated content");

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.update(username, postId, commentId, command))
                .isInstanceOf(CommentErrorException.class);
    }

    @Test
    @DisplayName("댓글수정_실패_게시글_댓글_관계_불일치")
    void update_invalid_relation() {
        Long postId = 1L;
        Long otherPostId = 2L;
        Long commentId = 10L;
        Long userId = 100L;

        Users user = createUser(userId, username, UserRole.USER);
        Board board = createBoard(1L);

        Post post = createPost(postId, board, user);
        Post otherPost = createPost(otherPostId, board, user);
        Comment comment = createComment(commentId, otherPost, user, "content");

        CommentCommand command = new CommentCommand("updated content");

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.update(username, postId, commentId, command))
                .isInstanceOf(PostErrorException.class);
    }

    @Test
    @DisplayName("댓글수정_실패_작성자_아님")
    void update_forbidden() {
        Long postId = 1L;
        Long commentId = 10L;

        Users user = createUser(100L, username, UserRole.USER);
        Users otherUser = createUser(200L, "otherUser", UserRole.USER);

        Board board = createBoard(1L);
        Post post = createPost(postId, board, otherUser);
        Comment comment = createComment(commentId, post, otherUser, "content");

        CommentCommand command = new CommentCommand("updated content");

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.update(username, postId, commentId, command))
                .isInstanceOf(AuthErrorException.class);
    }

    @Test
    @DisplayName("댓글수정_실패_삭제된_댓글")
    void update_deleted_comment() {
        Long postId = 1L;
        Long commentId = 10L;
        Long userId = 100L;

        Users user = createUser(userId, username, UserRole.USER);
        Board board = createBoard(1L);
        Post post = createPost(postId, board, user);
        Comment comment = createComment(commentId, post, user, "content");
        comment.delete();

        CommentCommand command = new CommentCommand("updated content");

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.update(username, postId, commentId, command))
                .isInstanceOf(CommentErrorException.class);
    }

    @Test
    @DisplayName("댓글수정_실패_삭제된_게시글")
    void update_deleted_post() {
        Long postId = 1L;
        Long commentId = 10L;
        Long userId = 100L;

        Users user = createUser(userId, username, UserRole.USER);
        Board board = createBoard(1L);
        Post post = createPost(postId, board, user);
        post.delete();

        CommentCommand command = new CommentCommand("updated content");

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> commentService.update(username, postId, commentId, command))
                .isInstanceOf(PostErrorException.class);
    }

    @Test
    @DisplayName("댓글삭제_성공")
    void delete() {
        Long postId = 1L;
        Long commentId = 10L;
        Long userId = 100L;

        Users user = createUser(userId, username, UserRole.USER);
        Board board = createBoard(1L);
        Post post = createPost(postId, board, user);
        Comment comment = createComment(commentId, post, user, "content");

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        commentService.delete(postId, commentId, username);

        assertThat(comment.getStatus()).isEqualTo(CommentStatus.DELETED);

        verify(usersRepository).findByUsername(username);
        verify(postRepository).findById(postId);
        verify(commentRepository).findById(commentId);
    }

    @Test
    @DisplayName("댓글삭제_실패_게시글_없음")
    void delete_post_not_found() {
        Long postId = 1L;
        Long commentId = 10L;
        Long userId = 100L;

        Users user = createUser(userId, username, UserRole.USER);

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.delete(postId, commentId, username))
                .isInstanceOf(PostErrorException.class);
    }

    @Test
    @DisplayName("댓글삭제_실패_댓글_없음")
    void delete_comment_not_found() {
        Long postId = 1L;
        Long commentId = 10L;
        Long userId = 100L;

        Users user = createUser(userId, username, UserRole.USER);
        Board board = createBoard(1L);
        Post post = createPost(postId, board, user);

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.delete(postId, commentId, username))
                .isInstanceOf(CommentErrorException.class);
    }

    @Test
    @DisplayName("댓글삭제_실패_게시글_댓글_관계_불일치")
    void delete_invalid_relation() {
        Long postId = 1L;
        Long otherPostId = 2L;
        Long commentId = 10L;
        Long userId = 100L;

        Users user = createUser(userId, username, UserRole.USER);
        Board board = createBoard(1L);

        Post post = createPost(postId, board, user);
        Post otherPost = createPost(otherPostId, board, user);
        Comment comment = createComment(commentId, otherPost, user, "content");

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.delete(postId, commentId, username))
                .isInstanceOf(PostErrorException.class);
    }

    @Test
    @DisplayName("댓글삭제_성공_관리자")
    void delete_admin() {
        Long postId = 1L;
        Long commentId = 10L;

        Users admin = createUser(100L, username, UserRole.ADMIN);
        Users otherUser = createUser(200L, "otherUser", UserRole.USER);

        Board board = createBoard(1L);
        Post post = createPost(postId, board, otherUser);
        Comment comment = createComment(commentId, post, otherUser, "content");

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(admin));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        commentService.delete(postId, commentId, username);

        assertThat(comment.getStatus()).isEqualTo(CommentStatus.DELETED);

        verify(usersRepository).findByUsername(username);
        verify(postRepository).findById(postId);
        verify(commentRepository).findById(commentId);
    }

    @Test
    @DisplayName("댓글삭제_실패_작성자_아님")
    void delete_forbidden() {
        Long postId = 1L;
        Long commentId = 10L;

        Users user = createUser(100L, username, UserRole.USER);
        Users otherUser = createUser(200L, "otherUser", UserRole.USER);

        Board board = createBoard(1L);
        Post post = createPost(postId, board, otherUser);
        Comment comment = createComment(commentId, post, otherUser, "content");

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.delete(postId, commentId, username))
                .isInstanceOf(AuthErrorException.class);
    }

    @Test
    @DisplayName("댓글삭제_실패_삭제된_게시글")
    void delete_deleted_post() {
        Long postId = 1L;
        Long commentId = 10L;
        Long userId = 100L;

        Users user = createUser(userId, username, UserRole.USER);
        Board board = createBoard(1L);
        Post post = createPost(postId, board, user);
        post.delete();

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> commentService.delete(postId, commentId, username))
                .isInstanceOf(PostErrorException.class);
    }

    @Test
    @DisplayName("댓글삭제_실패_삭제된_댓글")
    void delete_deleted_comment() {
        Long postId = 1L;
        Long commentId = 10L;
        Long userId = 100L;

        Users user = createUser(userId, username, UserRole.USER);
        Board board = createBoard(1L);
        Post post = createPost(postId, board, user);
        Comment comment = createComment(commentId, post, user, "content");
        comment.delete();

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.delete(postId, commentId, username))
                .isInstanceOf(CommentErrorException.class);
    }

    private Users createUser(Long userId, String username, UserRole role) {
        Users user = new Users(
                username,
                "nickname",
                "password",
                username + "@test.com",
                "01012341234",
                role
        );
        ReflectionTestUtils.setField(user, "userId", userId);
        return user;
    }

    private Board createBoard(Long boardId) {
        Board board = Board.create(
                "자유게시판",
                "free",
                true,
                false,
                false,
                0,
                UserRole.USER
        );
        ReflectionTestUtils.setField(board, "id", boardId);
        return board;
    }

    private Post createPost(Long postId, Board board, Users user) {
        Post post = Post.create(
                board,
                user,
                "title",
                "post content",
                ContentFormat.MARKDOWN
        );
        ReflectionTestUtils.setField(post, "id", postId);
        return post;
    }

    private Comment createComment(Long commentId, Post post, Users user, String content) {
        Comment comment = Comment.create(
                post,
                user,
                content
        );
        ReflectionTestUtils.setField(comment, "id", commentId);
        return comment;
    }

}