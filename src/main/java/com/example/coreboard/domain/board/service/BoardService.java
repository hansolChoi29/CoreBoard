package com.example.coreboard.domain.board.service;

import com.example.coreboard.domain.board.dto.*;
import com.example.coreboard.domain.board.dto.command.BoardCreateCommand;
import com.example.coreboard.domain.board.dto.command.BoardGetOneCommand;
import com.example.coreboard.domain.board.dto.command.BoardUpdateCommand;
import com.example.coreboard.domain.board.dto.response.BoardSummaryKeysetResponse;
import com.example.coreboard.domain.board.dto.response.BoardSummaryResponse;
import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;
import com.example.coreboard.domain.common.response.PageResponse;
import com.example.coreboard.domain.common.response.CursorResponse;
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

        public CursorResponse<BoardSummaryKeysetResponse> findAll(
                        String cursorTitle,
                        Long cursorId,
                        int size,
                        String sort) {
                                
                // 첫페이지/다음페이지 분기
                List<Board> result = (cursorTitle == null || cursorId == null)
                                ? boardRepository.findFirstPage(size + 1)
                                : boardRepository.findNextPage(cursorTitle, cursorId, size + 1);

                // 11개 왔으면 다음 있음 마지막 1개 버림 10개 이하로 왔으면 마지막 페이지
                boolean hasNext = result.size() > size;
                if (hasNext) {
                        result = result.subList(0, size);
                }

                List<BoardSummaryKeysetResponse> contents = result.stream()
                .map(b -> new BoardSummaryKeysetResponse(
                        b.getId(),
                        b.getUserId(),
                        b.getTitle(),
                        b.getCreatedDate()
                ))
                .toList();

                String nextCursorTitle = hasNext ? result.get(result.size() - 1).getTitle() : null;
                Long nextCursorId = hasNext?result.get(result.size()-1).getId() : null;
                
                return new CursorResponse<>(contents, nextCursorTitle, nextCursorId, hasNext);
        }
        // TODO : keyset - 부하테스트 2차

        /*
         * DAO -> DB에서 데이터 꺼내오는 담당자
         * 서비스 코드에 SQL이 섞이면 더러워지고 유지보수가 힘드니까 SQL을 한 곳(DAO)으로 몰아넣자
         * 
         * repository -> 도메인을 보관/꺼내주는 창고 담당자
         * 서비스가 DB 중심으로 사고하면 비즈니스 로직이 테이블에 끌려다님
         * 그래서 서비스는 도메인 기준으로만 말하게 하자
         */

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