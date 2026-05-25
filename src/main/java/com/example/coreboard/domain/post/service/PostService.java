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
import com.example.coreboard.domain.board.exception.BoardErrorCode;
import com.example.coreboard.domain.board.exception.BoardErrorException;
import com.example.coreboard.global.response.OffsetPageResponse;
import com.example.coreboard.global.response.PageInfo;
import com.example.coreboard.global.response.SliceInfo;
import com.example.coreboard.global.response.SliceResponse;
import com.example.coreboard.domain.post.validation.PostAttachmentPolicyValidator;
import com.example.coreboard.domain.post.validation.PostAttachmentUpdatePolicy;
import com.example.coreboard.domain.post.dto.command.CreatePostCommand;
import com.example.coreboard.domain.post.dto.command.DeletePostCommand;
import com.example.coreboard.domain.post.dto.command.GetOnePostCommand;
import com.example.coreboard.domain.post.dto.command.UpdatePostCommand;
import com.example.coreboard.domain.post.dto.query.PostSummaryProjection;
import com.example.coreboard.domain.post.dto.response.PostAttachmentResponse;
import com.example.coreboard.domain.post.dto.response.PostSummaryResponse;
import com.example.coreboard.domain.post.dto.result.CreatePostResult;
import com.example.coreboard.domain.post.dto.result.GetOnePostResult;
import com.example.coreboard.domain.post.dto.result.UpdatePostResult;
import com.example.coreboard.domain.post.entity.Post;
import com.example.coreboard.domain.post.entity.PostStatus;
import com.example.coreboard.domain.post.repository.PostRepository;
import com.example.coreboard.domain.auth.exception.AuthErrorException;
import com.example.coreboard.domain.post.exception.PostErrorException;
import com.example.coreboard.domain.users.entity.UserRole;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.coreboard.domain.auth.exception.AuthErrorCode.*;
import static com.example.coreboard.domain.post.exception.PostErrorCode.*;

import java.util.List;

@Service
public class PostService {
    private static final Logger log = LoggerFactory.getLogger(PostService.class);
    // 게시글 CRUD 담당

    // PostViewCountService
    // Redis 조회수 증가, 중복 조회 방지 담당

    // PopularPostService
    // Redis ZSET 기반 인기글 조회 담당

    // PostViewCountSyncService
    // Redis delete를 DB view_count에 반영 담당

    // 조회수 INCR, 인기글 ZSET 구조
    // INCR : 숫자 1 증가 명령
    // ZSET : 점수로 정렬되는 Redis 자료구조
    private final PostViewCountService postViewCountService;

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final UsersRepository usersRepository;
    private final CommentService commentService;
    private final AttachmentService attachmentService;
    private final AttachmentRepository attachmentRepository;

    public PostService(
            PostViewCountService postViewCountService,
            PostRepository postRepository,
            BoardRepository boardRepository,
            UsersRepository usersRepository,
            CommentService commentService,
            AttachmentService attachmentService,
            AttachmentRepository attachmentRepository
    ) {
        this.postViewCountService = postViewCountService;
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
            throw new AuthErrorException(FORBIDDEN);
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
        // 조회수 증가를 넣어야 하는데 음.. 실패해도 조회는 성공되게
        // getOne은 읽기 전용인데, redis는 write하고 있다
        // 게시글 DB 조회는 readOnly 트랜잭션으로 처리하고
        // 조회수 증가는 redis에 별도 site-effect로 기록한다
        // db write 부하는 즉시 발생시키지 않고 scheduler가 나중에 반영되도록 분리한다

        try{
            postViewCountService.increaseIfFirstView(command.id(), command.viewerKey());
        }catch(Exception e){
            log.warn("조회수 증가 실패. postId = {}", command.id(), e);
        }

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
    public SliceResponse<PostSummaryResponse> getBoardAll(
            Long boardId,
            int page,
            int size
    ) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        Slice<Post> postSlice = postRepository.findAllByBoardId(
                boardId,
                PostStatus.PUBLISHED,
                pageable
        );

        List<PostSummaryResponse> contents = postSlice.getContent().stream()
                .map(post -> new PostSummaryResponse(
                        post.getId(),
                        post.getUser().getNickname(),
                        post.getTitle(),
                        post.getCreatedAt(),
                        post.getUpdatedAt()
                ))
                .toList();

        return new SliceResponse<>(contents,
                new SliceInfo(
                        postSlice.getSize(),
                        postSlice.getNumberOfElements(),
                        postSlice.hasNext()
                ));
    }

    @Transactional(readOnly = true)
    public SliceResponse<PostSummaryResponse> searchPosts(
            int page,
            int size,
            String keyword
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Slice<PostSummaryProjection> postSlice = postRepository.searchAllPosts(
                PostStatus.PUBLISHED.name(),
                keyword.trim(),
                pageable
        );

        List<PostSummaryResponse> contents = postSlice.getContent().stream()
                .map(post -> new PostSummaryResponse(
                        post.getId(),
                        post.getWriterName(),
                        post.getTitle(),
                        post.getCreatedAt(),
                        post.getUpdatedAt()
                ))
                .toList();

        return new SliceResponse<>(
                contents,
                new SliceInfo(
                        postSlice.getSize(),
                        postSlice.getNumberOfElements(),
                        postSlice.hasNext()
                )
        );
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