package com.example.coreboard.domain.post.service;

import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.comment.dto.query.GetCommentQuery;
import com.example.coreboard.domain.comment.dto.response.GetAllCommentResponse;
import com.example.coreboard.domain.comment.service.CommentService;
import com.example.coreboard.domain.common.exception.board.BoardErrorCode;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;
import com.example.coreboard.domain.common.response.OffsetPageResponse;
import com.example.coreboard.domain.common.response.PageInfo;
import com.example.coreboard.domain.common.response.SliceResponse;
import com.example.coreboard.domain.common.validation.PostAttachmentPolicyValidator;
import com.example.coreboard.domain.post.dto.command.CreatePostCommand;
import com.example.coreboard.domain.post.dto.command.DeletePostCommand;
import com.example.coreboard.domain.post.dto.command.GetOnePostCommand;
import com.example.coreboard.domain.post.dto.command.UpdatePostCommand;
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
import com.example.coreboard.domain.common.response.CursorResponse;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static com.example.coreboard.domain.common.exception.auth.AuthErrorCode.*;
import static com.example.coreboard.domain.common.exception.post.PostErrorCode.*;

import java.util.List;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final UsersRepository usersRepository;
    private final CommentService commentService;

    public PostService(
            PostRepository postRepository,
            BoardRepository boardRepository,
            UsersRepository usersRepository,
            CommentService commentService
    ) {
        this.postRepository = postRepository;
        this.boardRepository = boardRepository;
        this.usersRepository = usersRepository;
        this.commentService = commentService;
    }

    @Transactional
    public CreatePostResult create(
            CreatePostCommand command,
            String username
    ) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new AuthErrorException(NOT_FOUND));
        if (postRepository.existsByTitle(command.title())) {
            throw new PostErrorException(TITLE_DUPLICATED);
        }
        Board board = boardRepository.findById(command.boardId())
                .orElseThrow(() -> new BoardErrorException(BoardErrorCode.BOARD_NOT_FOUND));

        PostAttachmentPolicyValidator.validate(board, command.attachmentIds());

        Post post = Post.create(
                board,
                user,
                command.title(),
                command.content(),
                ContentFormat.MARKDOWN
        );
        Post saved = postRepository.save(post);

        return new CreatePostResult(saved.getId());
    }

    // TODO : 댓글 조회
    @Transactional(readOnly = true)
    public GetOnePostResult getOne(GetOnePostCommand command) {
        Post post = postRepository.findById(command.id())
                .orElseThrow(() -> new PostErrorException(POST_NOT_FOUND));

        SliceResponse<GetAllCommentResponse> comments = commentService.getAll(new GetCommentQuery(command.id(), 0, 10));

        return new GetOnePostResult(
                post.getId(),
                post.getUser().getUserId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                comments
        );
    }

    @Transactional(readOnly = true)
    public OffsetPageResponse<PostSummaryResponse> getAll(
            Long boardId,
            int page,
            int size,
            String sort
    ) {
        Sort sortObj = "desc".equalsIgnoreCase(sort) ?
                Sort.by(Sort.Direction.DESC, "createdAt")
                : Sort.by(Sort.Direction.ASC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<Post> postPage = postRepository.findAllByBoardId(
                boardId,
                PostStatus.PUBLISHED,
                pageable
        );
        List<PostSummaryResponse> contents = postPage.getContent().stream()
                .map(post -> new PostSummaryResponse(
                        post.getId(),
                        post.getUser().getNickname(),
                        post.getTitle(),
                        post.getCreatedAt(),
                        post.getUpdatedAt()
                )).toList();
        PageInfo pageInfo = new PageInfo(
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalElements(),
                postPage.getTotalPages()
        );
        return new OffsetPageResponse<>(contents, pageInfo);
    }

    @Transactional
    public UpdatePostResult update(UpdatePostCommand command) {
        Users user = usersRepository.findByUsername(command.username())
                .orElseThrow(() -> new AuthErrorException(NOT_FOUND));

        Post post = postRepository.findById(command.id())
                .orElseThrow(() -> new PostErrorException(POST_NOT_FOUND));

        if (!post.getUser().getUserId().equals(user.getUserId())) {
            throw new AuthErrorException(FORBIDDEN);
        }
        post.update(
                command.title(),
                command.content(),
                command.contentFormat());

        return new UpdatePostResult(
                post.getId(),
                post.getCreatedAt(),
                post.getUpdatedAt());
    }

    @Transactional
    public void delete(DeletePostCommand command) {
        Users user = usersRepository.findByUsername(command.username())
                .orElseThrow(() -> new AuthErrorException(NOT_FOUND));
        Post post = postRepository.findById(command.id())
                .orElseThrow(() -> new PostErrorException(POST_NOT_FOUND));

        if (!post.getUser().getUserId().equals(user.getUserId())) {
            throw new AuthErrorException(FORBIDDEN);
        }
        post.delete();
    }

    @Transactional
    public CursorResponse<PostSummaryResponse> search(String keyword) {
        List<Post> posts = postRepository.searchByKeyword(keyword);
        List<PostSummaryResponse> contents = posts.stream().map(
                post -> new PostSummaryResponse(
                        post.getId(),
                        post.getUser().getNickname(),
                        post.getTitle(),
                        post.getCreatedAt(),
                        post.getUpdatedAt()
                )).toList();

        return new CursorResponse<>(
                contents,
                null,
                null,
                false
        );
    }
}