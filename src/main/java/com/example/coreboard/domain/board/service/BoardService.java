package com.example.coreboard.domain.board.service;

import com.example.coreboard.domain.board.dto.command.UpdateBoardCommand;
import com.example.coreboard.domain.board.dto.response.GetBoardListResponse;
import com.example.coreboard.domain.board.dto.result.CreateBoardResult;
import com.example.coreboard.domain.board.dto.result.GetOneBoardResult;
import com.example.coreboard.domain.board.dto.command.CreateBoardCommand;
import com.example.coreboard.domain.board.dto.query.GetBoardListQuery;
import com.example.coreboard.domain.board.dto.command.GetOneBoardCommand;
import com.example.coreboard.domain.board.dto.result.UpdateBoardResult;
import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.common.exception.auth.AuthErrorCode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.board.BoardErrorCode;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;
import com.example.coreboard.domain.common.response.OffsetPageResponse;
import com.example.coreboard.domain.common.response.PageInfo;
import com.example.coreboard.domain.post.dto.response.PostSummaryResponse;
import com.example.coreboard.domain.post.entity.Post;
import com.example.coreboard.domain.post.repository.PostRepository;
import com.example.coreboard.domain.users.entity.UserRole;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final UsersRepository usersRepository;
    private final PostRepository postRepository;

    public BoardService(
            BoardRepository boardRepository,
            UsersRepository usersRepository,
            PostRepository postRepository
    ) {
        this.boardRepository = boardRepository;
        this.usersRepository = usersRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public CreateBoardResult create(CreateBoardCommand command, String username) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new AuthErrorException(AuthErrorCode.NOT_FOUND));
        // slug는 주소라서 중복이면 절대 안 된다
        if (user.getRole() != UserRole.ADMIN) {
            throw new AuthErrorException(AuthErrorCode.FORBIDDEN);
        }
        if (boardRepository.existsBySlug(command.slug())) {
            throw new BoardErrorException(BoardErrorCode.BOARD_SLUG_DUPLICATE);
        }
        if (boardRepository.existsByName(command.name())) {
            throw new BoardErrorException(BoardErrorCode.BOARD_NAME_DUPLICATE);
        }
        Board board = Board.create(
                command.name(),
                command.slug(),
                command.commentEnabled(),
                command.answerAcceptedEnabled(),
                command.requireAttachment(),
                command.maxAttachmentCount(),
                command.maxContentLength()
        );
        boardRepository.save(board);

        return new CreateBoardResult(board.getId());
    }

    @Transactional(readOnly = true)
    public GetOneBoardResult getOne(GetOneBoardCommand command) {
        Board board = boardRepository.findById(command.id())
                .orElseThrow(() -> new BoardErrorException(BoardErrorCode.BOARD_NOT_FOUND));
        List<PostSummaryResponse> posts = postRepository.findByBoardId(command.id())
                .stream()
                .map(post -> new PostSummaryResponse(
                        post.getId(),
                        post.getUser().getNickname(),
                        post.getTitle(),
                        post.getCreatedAt(),
                        post.getUpdatedAt()
                )).toList();
        return new GetOneBoardResult(
                board.getId(),
                board.getName(),
                board.getSlug(),
                board.isAnswerAcceptedEnabled(),
                board.isCommentEnabled(),
                board.isRequireAttachment(),
                board.getMaxAttachmentCount(),
                board.getMaxContentLength(),
                board.getRequiredWriteRole(),
                posts
        );
    }

    @Transactional
    public OffsetPageResponse<GetBoardListResponse> getAll(GetBoardListQuery query) {
        PageRequest pageRequest = PageRequest.of(
                query.page(),
                query.size(),
                Sort.by(query.direction(), "id")
        );
        Page<Board> boardPage = boardRepository.findAll(pageRequest);
        List<GetBoardListResponse> content = boardPage.getContent()
                .stream()
                .map(board -> new GetBoardListResponse(
                        board.getId(),
                        board.getName(),
                        board.getSlug()
                )).toList();
        PageInfo pageInfo = new PageInfo(
                boardPage.getNumber(),
                boardPage.getSize(),
                boardPage.getTotalElements(),
                boardPage.getTotalPages()
        );
        return new OffsetPageResponse<>(content, pageInfo);
    }

    @Transactional
    public UpdateBoardResult update(
            UpdateBoardCommand command,
            String username,
            Long id
    ) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new AuthErrorException(AuthErrorCode.NOT_FOUND));
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new BoardErrorException(BoardErrorCode.BOARD_NOT_FOUND));
        if (user.getRole() != UserRole.ADMIN) {
            throw new AuthErrorException(AuthErrorCode.FORBIDDEN);
        }
        if (boardRepository.existsByName(command.name())) {
            throw new BoardErrorException(BoardErrorCode.BOARD_NAME_DUPLICATE);
        }
        if (boardRepository.existsBySlug(command.slug())) {
            throw new BoardErrorException(BoardErrorCode.BOARD_SLUG_DUPLICATE);
        }
        board.update(
                command.name(),
                command.slug(),
                command.answerAcceptedEnabled(),
                command.commentEnabled(),
                command.requireAttachment(),
                command.maxAttachmentCount(),
                command.maxContentLength()
        );
        return new UpdateBoardResult(board.getId());
    }
}
