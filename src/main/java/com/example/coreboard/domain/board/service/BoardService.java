package com.example.coreboard.domain.board.service;

import com.example.coreboard.domain.board.dto.*;
import com.example.coreboard.domain.board.dto.command.BoardCreateCommand;
import com.example.coreboard.domain.board.dto.command.BoardGetOneCommand;
import com.example.coreboard.domain.board.dto.command.BoardUpdateCommand;
import com.example.coreboard.domain.board.dto.response.BoardSummaryResponse;
import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;
import com.example.coreboard.domain.common.response.PageResponse;
import com.example.coreboard.domain.users.entity.Users;
import com.example.coreboard.domain.users.repository.UsersRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.coreboard.domain.common.exception.auth.AuthErrorCode.*;
import static com.example.coreboard.domain.common.exception.board.BoardErrorCode.*;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
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

        public PageResponse<BoardSummaryResponse> findAll(int page, int size, String sort) {
                Sort.Direction direction = sort.equalsIgnoreCase("asc")
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC;

                Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "title"));

                Page<Board> result = boardRepository.findAll(pageable);

                List<BoardSummaryResponse> contents = new ArrayList<>();

                for (Board board : result.getContent()) {
                        contents.add(new BoardSummaryResponse(
                                        board.getId(),
                                        board.getUserId(),
                                        board.getTitle(),
                                        board.getCreatedDate()));
                }

                PageResponse<BoardSummaryResponse> body = new PageResponse<>(
                                contents,
                                result.getNumber(),
                                result.getSize(),
                                result.getTotalElements());

                return body;
        }

        // TODO : keyset - 부하테스트 2차
        

        @Transactional
        public BoardUpdatedDto update(
                        BoardUpdateCommand boardUpdatedCommad) {
                Users user = usersRepository.findByUsername(boardUpdatedCommad.getUsername())
                                .orElseThrow(() -> new AuthErrorException(NOT_FOUND));

                Board board = boardRepository.findById(boardUpdatedCommad.getId())
                                .orElseThrow(() -> new BoardErrorException(POST_NOT_FOUND));

                if (board.getUserId() != user.getUserId()) {
                        throw new AuthErrorException(FORBIDDEN);
                }

                board.update(
                                boardUpdatedCommad.getTitle(),
                                boardUpdatedCommad.getContent());
                return new BoardUpdatedDto(
                                board.getId());
        }

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