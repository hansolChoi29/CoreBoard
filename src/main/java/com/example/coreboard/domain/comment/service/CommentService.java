package com.example.coreboard.domain.comment.service;

import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.comment.dto.command.CommentCommand;
import com.example.coreboard.domain.comment.dto.query.GetCommentQuery;
import com.example.coreboard.domain.comment.dto.request.CommentRequest;
import com.example.coreboard.domain.comment.dto.response.GetAllCommentResponse;
import com.example.coreboard.domain.comment.dto.result.CommentResult;
import com.example.coreboard.domain.comment.entity.Comment;
import com.example.coreboard.domain.comment.entity.CommentStatus;
import com.example.coreboard.domain.comment.repository.CommentRepository;
import com.example.coreboard.domain.common.exception.auth.AuthErrorCode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.comment.CommentErrorCode;
import com.example.coreboard.domain.common.exception.comment.CommentErrorException;
import com.example.coreboard.domain.common.exception.post.PostErrorCode;
import com.example.coreboard.domain.common.exception.post.PostErrorException;
import com.example.coreboard.domain.common.response.SliceResponse;
import com.example.coreboard.domain.post.entity.Post;
import com.example.coreboard.domain.post.repository.PostRepository;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UsersRepository usersRepository;

    public CommentService(
            CommentRepository commentRepository,
            PostRepository postRepository,
            UsersRepository usersRepository
    ) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.usersRepository = usersRepository;
    }

    @Transactional
    public CommentResult create(
            Long postId,
            String username,
            CommentCommand command
    ) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new AuthErrorException(AuthErrorCode.NOT_FOUND));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostErrorException(PostErrorCode.POST_NOT_FOUND));
        if (post.isDeleted()) {
            throw new PostErrorException(PostErrorCode.POST_NOT_FOUND);
        }
        Board board = post.getBoard();

        if (!board.isCommentEnabled()) {
            throw new CommentErrorException(CommentErrorCode.COMMENT_NOT_ALLOWED);
        }

        Comment comment = Comment.create(
                post,
                user,
                command.content()
        );
        commentRepository.save(comment);
        return new CommentResult(comment.getId());
    }

    @Transactional(readOnly = true)
    public SliceResponse<GetAllCommentResponse> getAll(GetCommentQuery query) {
        Pageable pageable = PageRequest.of(
                query.page(),
                query.size(),
                Sort.by(Sort.Direction.DESC, "createdDate")
        );
        if (!postRepository.existsById(query.postId())) {
            throw new PostErrorException(PostErrorCode.POST_NOT_FOUND);
        }
        Slice<Comment> sliceComment = commentRepository.findByPostIdAndStatus(
                query.postId(),
                CommentStatus.ACTIVE,
                pageable
        );

        Slice<GetAllCommentResponse> commentSlice = sliceComment.map(GetAllCommentResponse::from);
        return SliceResponse.from(commentSlice);
    }


    @Transactional
    public CommentResult update(
            String username,
            Long postId,
            Long id,
            CommentCommand command
    ) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new AuthErrorException(AuthErrorCode.NOT_FOUND));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostErrorException(PostErrorCode.POST_NOT_FOUND));
        if (post.isDeleted()) {
            throw new PostErrorException(PostErrorCode.POST_NOT_FOUND);
        }

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentErrorException(CommentErrorCode.COMMENT_NOT_FOUND));
        if (!comment.getPost().getId().equals(postId)) {
            throw new PostErrorException(PostErrorCode.INVALID_RELATION);
        }
        if (!comment.getUser().getUserId().equals(user.getUserId())) {
            throw new AuthErrorException(AuthErrorCode.FORBIDDEN);
        }

        comment.update(command.content());

        return new CommentResult(comment.getId());
    }
}
