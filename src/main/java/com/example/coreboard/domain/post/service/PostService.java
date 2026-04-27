package com.example.coreboard.domain.post.service;

import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.common.exception.board.BoardErrorCode;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;
import com.example.coreboard.domain.post.dto.*;
import com.example.coreboard.domain.post.dto.command.PostCreateCommand;
import com.example.coreboard.domain.post.dto.command.PostGetOneCommand;
import com.example.coreboard.domain.post.dto.command.PostUpdateCommand;
import com.example.coreboard.domain.post.dto.response.PostSummaryKeysetResponse;
import com.example.coreboard.domain.post.entity.Post;
import com.example.coreboard.domain.post.repository.PostRepository;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.post.PostErrorException;
import com.example.coreboard.domain.common.response.CursorResponse;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
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

    public PostService(
            PostRepository postRepository,
            BoardRepository boardRepository,
            UsersRepository usersRepository
    ) {
        this.postRepository = postRepository;
        this.boardRepository = boardRepository;
        this.usersRepository = usersRepository;
    }

    @Transactional
    public PostCreateDto create(
            PostCreateCommand boardCreateCommand,
            String username
    )  {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new AuthErrorException(NOT_FOUND));

        if (postRepository.existsByTitle(boardCreateCommand.getTitle())) {
            throw new PostErrorException(TITLE_DUPLICATED);
        }

        Board board = boardRepository.findById(boardCreateCommand.getBoardId())
                .orElseThrow(() -> new BoardErrorException(BoardErrorCode.BOARD_NOT_FOUND));

        Post post = Post.create(
                board,
                user,
                boardCreateCommand.getTitle(),
                boardCreateCommand.getContent(),
                boardCreateCommand.getContentFormat());

        Post saved = postRepository.save(post);

        return new PostCreateDto(
                saved.getId(),
                saved.getUser().getUserId(),
                saved.getTitle(),
                saved.getContent(),
                saved.getCreatedDate());
    }

    public PostGetOneDto findOne(PostGetOneCommand boardGetOneCommand) {
        Post board = postRepository.findById(boardGetOneCommand.getId())
                .orElseThrow(() -> new PostErrorException(POST_NOT_FOUND));

        return new PostGetOneDto(
                board.getId(),
                board.getUser().getUserId(),
                board.getTitle(),
                board.getContent(),
                board.getCreatedDate(),
                board.getLastModifiedDate()
        );
    }

    public CursorResponse<PostSummaryKeysetResponse> findAll(
            String cursorTitle,
            Long cursorId,
            int size,
            String sort
    ) {
        Pageable pageable = PageRequest.of(0, size + 1);
        boolean isDesc = "desc".equalsIgnoreCase(sort);
        List<Post> result = (cursorTitle == null || cursorId == null)
                ? (isDesc ? postRepository.findFirstPageDesc(pageable)
                : postRepository.findFirstPageAsc(pageable))
                : (isDesc ? postRepository.findNextPageDesc(cursorTitle, cursorId, pageable)
                : postRepository.findNextPageAsc(cursorTitle, cursorId, pageable));

        boolean hasNext = result.size() > size;
        if (hasNext) {
            result = result.subList(0, size);
        }

        List<PostSummaryKeysetResponse> contents = result.stream()
                .map(b -> new PostSummaryKeysetResponse(
                        b.getId(),
                        b.getUser().getUserId(),
                        b.getTitle(),
                        b.getCreatedDate()))
                .toList();

        String nextCursorTitle = hasNext ? result.get(result.size() - 1).getTitle() : null;
        Long nextCursorId = hasNext ? result.get(result.size() - 1).getId() : null;

        return new CursorResponse<>(
                contents,
                nextCursorTitle,
                nextCursorId,
                hasNext
        );
    }

    @Transactional
    public PostUpdatedDto update(PostUpdateCommand boardUpdatedCommad) {
        Users user = usersRepository.findByUsername(boardUpdatedCommad.getUsername())
                .orElseThrow(() -> new AuthErrorException(NOT_FOUND));

        Post board = postRepository.findById(boardUpdatedCommad.getId())
                .orElseThrow(() -> new PostErrorException(POST_NOT_FOUND));

        if (!board.getUser().getUserId().equals(user.getUserId())) {
            throw new AuthErrorException(FORBIDDEN);
        }

        board.update(
                boardUpdatedCommad.getTitle(),
                boardUpdatedCommad.getContent(),
                boardUpdatedCommad.getContentFormat());
        return new PostUpdatedDto(
                board.getId());
    }

    @Transactional
    public void delete(
            String username,
            Long id
    ) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new AuthErrorException(NOT_FOUND));

        postRepository.findById(id)
                .filter(board -> {
                    if (!board.getUser().getUserId().equals(user.getUserId())) {
                        throw new AuthErrorException(FORBIDDEN);
                    }
                    return true;
                })
                .ifPresent(postRepository::delete);
    }

    @Transactional
    public CursorResponse<PostSummaryKeysetResponse> search(String keyword) {
        List<Post> boards = postRepository.searchByKeyword(keyword);

        List<PostSummaryKeysetResponse> contents = boards.stream().map(
                board -> new PostSummaryKeysetResponse(
                        board.getId(),
                        board.getUser().getUserId(),
                        board.getTitle(),
                        board.getCreatedDate()
                )
        ).toList();

        return new CursorResponse<>(
                contents,
                null,
                null,
                false
        );
    }
}