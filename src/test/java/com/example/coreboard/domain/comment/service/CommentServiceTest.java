package com.example.coreboard.domain.comment.service;

import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.comment.dto.command.CreateCommentCommand;
import com.example.coreboard.domain.comment.dto.query.GetCommentQuery;
import com.example.coreboard.domain.comment.dto.response.GetAllCommentResponse;
import com.example.coreboard.domain.comment.dto.result.CreateCommentResult;
import com.example.coreboard.domain.comment.entity.Comment;
import com.example.coreboard.domain.comment.entity.CommentStatus;
import com.example.coreboard.domain.comment.repository.CommentRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    @DisplayName("댓글_생성_성공")
    void create() {
        Long postId = 1L;
        String content = "content";
        Board board = Board.create(
                "자유게시판",
                "free",
                true,
                false,
                false,
                0,
                UserRole.USER
        );
        Users user = new Users(
                username,
                "nickname",
                "password",
                "qwe@qwe.com",
                "01012341234",
                UserRole.USER
        );
        Post post = Post.create(
                board,
                user,
                "title",
                "post content",
                ContentFormat.MARKDOWN
        );
        Comment savedComment = Comment.create(
                post,
                user,
                content
        );
        CreateCommentCommand command = new CreateCommentCommand(content);

        given(usersRepository.findByUsername(username)).willReturn(Optional.of(user));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

        CreateCommentResult result = commentService.create(
                postId,
                username,
                command
        );
        assertThat(result).isNotNull();

        verify(usersRepository).findByUsername(username);
        verify(postRepository).findById(postId);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글_전체조회_성공")
    void getAll() {
        Long postId = 1L;
        GetCommentQuery query = new GetCommentQuery(postId, 0, 10);
        List<Comment> commentList = List.of();
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"));
        Slice<Comment> mockSlice = new SliceImpl<>(commentList, pageable, false);

        given(postRepository.existsById(anyLong())).willReturn(true);
        given(commentRepository.findByPostIdAndStatus(
                anyLong(),
                eq(CommentStatus.ACTIVE),
                any(Pageable.class)
        )).willReturn(mockSlice);

        SliceResponse<GetAllCommentResponse> response = commentService.getAll(query);
        assertThat(response).isNotNull();
        verify(postRepository).existsById(anyLong());
        verify(commentRepository).findByPostIdAndStatus(
                anyLong(),
                eq(CommentStatus.ACTIVE),
                any(Pageable.class)
        );
    }
}