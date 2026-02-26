package com.example.coreboard.domain.board.service;

import com.example.coreboard.domain.board.dto.*;
import com.example.coreboard.domain.board.dto.command.BoardCreateCommand;
import com.example.coreboard.domain.board.dto.command.BoardGetOneCommand;
import com.example.coreboard.domain.board.dto.command.BoardUpdateCommand;
import com.example.coreboard.domain.board.dto.response.BoardSummaryKeysetResponse;
import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;
import com.example.coreboard.domain.common.response.CursorResponse;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static com.example.coreboard.domain.common.exception.auth.AuthErrorCode.*;
import static com.example.coreboard.domain.common.exception.board.BoardErrorCode.*;

import java.util.List;

@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final UsersRepository usersRepository;

    public BoardService(
            BoardRepository boardRepository,
            UsersRepository usersRepository) {
        this.boardRepository = boardRepository;
        this.usersRepository = usersRepository;
    }

    @Transactional
    public BoardCreateDto create(
            BoardCreateCommand boardCreateCommand,
            String username) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new AuthErrorException(NOT_FOUND));

        if (boardRepository.existsByTitle(boardCreateCommand.getTitle())) {
            throw new BoardErrorException(TITLE_DUPLICATED);
        }

        Board board = Board.create(
                user.getUserId(),
                boardCreateCommand.getTitle(),
                boardCreateCommand.getContent());
        Board saved = boardRepository.save(board);

        return new BoardCreateDto(
                saved.getId(),
                saved.getUserId(),
                saved.getTitle(),
                saved.getContent(),
                saved.getCreatedDate());
    }

    public BoardGetOneDto findOne(
            BoardGetOneCommand boardGetOneCommand) {

        Board board = boardRepository.findById(boardGetOneCommand.getId())
                .orElseThrow(() -> new BoardErrorException(POST_NOT_FOUND));

        return new BoardGetOneDto(board.getId(), board.getUserId(), board.getTitle(), board.getContent(),
                board.getCreatedDate(), board.getLastModifiedDate());
    }

    public CursorResponse<BoardSummaryKeysetResponse> findAll(
            String cursorTitle,
            Long cursorId,
            int size,
            String sort
    ) {
        Pageable pageable = PageRequest.of(0, size + 1);
        boolean isDesc = "desc".equalsIgnoreCase(sort);
        List<Board> result = (cursorTitle == null || cursorId == null)
                ? (isDesc ? boardRepository.findFirstPageDesc(pageable)
                : boardRepository.findFirstPageAsc(pageable))
                : (isDesc ? boardRepository.findNextPageDesc(cursorTitle, cursorId, pageable)
                : boardRepository.findNextPageAsc(cursorTitle, cursorId, pageable));

        boolean hasNext = result.size() > size;
        if (hasNext) {
            result = result.subList(0, size);
        }

        List<BoardSummaryKeysetResponse> contents = result.stream()
                .map(b -> new BoardSummaryKeysetResponse(
                        b.getId(),
                        b.getUserId(),
                        b.getTitle(),
                        b.getCreatedDate()))
                .toList();

        String nextCursorTitle = hasNext ? result.get(result.size() - 1).getTitle() : null;
        Long nextCursorId = hasNext ? result.get(result.size() - 1).getId() : null;

        return new CursorResponse<>(contents, nextCursorTitle, nextCursorId, hasNext);
    }

    @Transactional
    public BoardUpdatedDto update(
            BoardUpdateCommand boardUpdatedCommad) {
        Users user = usersRepository.findByUsername(boardUpdatedCommad.getUsername())
                .orElseThrow(() -> new AuthErrorException(NOT_FOUND));

        Board board = boardRepository.findById(boardUpdatedCommad.getId())
                .orElseThrow(() -> new BoardErrorException(POST_NOT_FOUND));

        if (!board.getUserId().equals(user.getUserId())) {
            throw new AuthErrorException(FORBIDDEN);
        }

        board.update(
                boardUpdatedCommad.getTitle(),
                boardUpdatedCommad.getContent());
        return new BoardUpdatedDto(
                board.getId());
    }

    @Transactional
    public void delete(
            String username,
            Long id) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new AuthErrorException(NOT_FOUND));

        boardRepository.findById(id)
                .filter(board -> {
                    if (!board.getUserId().equals(user.getUserId())) {
                        throw new AuthErrorException(FORBIDDEN);
                    }
                    return true;
                })
                .ifPresent(boardRepository::delete);
    }
}