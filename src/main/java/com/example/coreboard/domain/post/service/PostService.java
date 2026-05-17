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
import com.example.coreboard.domain.common.exception.auth.AuthErrorCode;
import com.example.coreboard.domain.common.exception.board.BoardErrorCode;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;
import com.example.coreboard.domain.common.response.OffsetPageResponse;
import com.example.coreboard.domain.common.response.PageInfo;
import com.example.coreboard.domain.common.response.SliceResponse;
import com.example.coreboard.domain.common.validation.PostAttachmentPolicyValidator;
import com.example.coreboard.domain.common.validation.PostAttachmentUpdatePolicy;
import com.example.coreboard.domain.post.dto.command.CreatePostCommand;
import com.example.coreboard.domain.post.dto.command.DeletePostCommand;
import com.example.coreboard.domain.post.dto.command.GetOnePostCommand;
import com.example.coreboard.domain.post.dto.command.UpdatePostCommand;
import com.example.coreboard.domain.post.dto.response.PostAttachmentResponse;
import com.example.coreboard.domain.post.dto.response.PostSummaryResponse;
import com.example.coreboard.domain.post.dto.result.CreatePostResult;
import com.example.coreboard.domain.post.dto.result.GetOnePostResult;
import com.example.coreboard.domain.post.dto.result.UpdatePostResult;
import com.example.coreboard.domain.post.entity.Post;
import com.example.coreboard.domain.post.entity.PostStatus;
import com.example.coreboard.domain.post.repository.PostRepository;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.post.PostErrorException;
import com.example.coreboard.domain.users.entity.UserRole;
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
    private final AttachmentService attachmentService;
    private final AttachmentRepository attachmentRepository;

    public PostService(
            PostRepository postRepository,
            BoardRepository boardRepository,
            UsersRepository usersRepository,
            CommentService commentService,
            AttachmentService attachmentService,
            AttachmentRepository attachmentRepository
    ) {
        this.postRepository = postRepository;
        this.boardRepository = boardRepository;
        this.usersRepository = usersRepository;
        this.commentService = commentService;
        this.attachmentService = attachmentService;
        this.attachmentRepository = attachmentRepository;
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

        if (!board.canWrite(user.getRole())) {
            throw new AuthErrorException(AuthErrorCode.FORBIDDEN);
        }

        PostAttachmentPolicyValidator.validate(board, command.attachmentIds());

        Post post = Post.create(
                board,
                user,
                command.title(),
                command.content(),
                command.contentFormat()
        );
        Post saved = postRepository.save(post);
        attachmentService.confirm(command.attachmentIds(), saved, user);

        return new CreatePostResult(saved.getId());
    }

    @Transactional(readOnly = true)
    public GetOnePostResult getOne(GetOnePostCommand command) {
        Post post = postRepository.findByIdAndStatus(command.id(), PostStatus.PUBLISHED)
                .orElseThrow(() -> new PostErrorException(POST_NOT_FOUND));

        SliceResponse<GetAllCommentResponse> comments = commentService.getAll(new GetCommentQuery(command.id(), 0, 10));

        List<PostAttachmentResponse> attachments = attachmentRepository.findByPostIdAndStatus(
                        post.getId(),
                        AttachmentStatus.CONFIRMED
                ).stream()
                .map(attachment -> new PostAttachmentResponse(
                        attachment.getId(),
                        attachment.getOriginalFileName(),
                        attachment.getStoreUrl(),
                        attachment.getContentType(),
                        attachment.getFileSize()
                )).toList();

        return new GetOnePostResult(
                post.getId(),
                post.getUser().getUserId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                comments,
                attachments
        );
    }

    @Transactional(readOnly = true)
    public OffsetPageResponse<PostSummaryResponse> getAll(
            int page,
            int size,
            String sort
    ) {
        Sort sortObj = "desc".equalsIgnoreCase(sort)
                ? Sort.by(Sort.Direction.DESC, "createdAt")
                : Sort.by(Sort.Direction.ASC, "createdAt");

        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<Post> postPage = postRepository.findAllByStatus(
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

    @Transactional(readOnly = true)
    public OffsetPageResponse<PostSummaryResponse> getBoardAll(
            Long boardId,
            int page,
            int size,
            String sort,
            String keyword
    ) {
        Sort sortObj = "desc".equalsIgnoreCase(sort) ?
                Sort.by(Sort.Direction.DESC, "createdAt")
                : Sort.by(Sort.Direction.ASC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<Post> postPage;

        if (keyword == null || keyword.isBlank()) {
            postPage = postRepository.findAllByBoardId(
                    boardId,
                    PostStatus.PUBLISHED,
                    pageable
            );
        } else {
            postPage = postRepository.searchByBoardId(
                    boardId,
                    PostStatus.PUBLISHED,
                    keyword.trim(),
                    pageable
            );
        }

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

        Post post = postRepository.findByIdAndStatus(command.id(), PostStatus.PUBLISHED)
                .orElseThrow(() -> new PostErrorException(POST_NOT_FOUND));

        if (!post.isWrittenBy(user) && user.getRole() != UserRole.ADMIN) {
            throw new AuthErrorException(FORBIDDEN);
        }

        List<Attachment> currentAttachments = attachmentRepository.findByPostIdAndStatus(
                post.getId(),
                AttachmentStatus.CONFIRMED
        );

        PostAttachmentUpdatePolicy.validate(
                post.getBoard(),
                currentAttachments,
                command.keepAttachmentIds(),
                command.newAttachmentIds()
        );

        post.update(
                command.title(),
                command.content(),
                command.contentFormat()
        );

        attachmentService.updatePostAttachments(
                post,
                user,
                command.keepAttachmentIds(),
                command.newAttachmentIds()
        );

        return new UpdatePostResult(
                post.getId(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    @Transactional
    public void delete(DeletePostCommand command) {
        Users user = usersRepository.findByUsername(command.username())
                .orElseThrow(() -> new AuthErrorException(NOT_FOUND));

        Post post = postRepository.findByIdAndStatus(command.id(), PostStatus.PUBLISHED)
                .orElseThrow(() -> new PostErrorException(POST_NOT_FOUND));

        if (!post.isWrittenBy(user) && user.getRole() != UserRole.ADMIN) {
            throw new AuthErrorException(FORBIDDEN);
        }

        post.delete();
        attachmentService.markDeletedByPost(post.getId());
    }
}