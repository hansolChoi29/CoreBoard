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
            int size
    ) {
        Pageable pageable = PageRequest.of(0, size + 1);
        List<Board> result = (cursorTitle == null || cursorId == null)
                ? boardRepository.findFirstPage(pageable)
                : boardRepository.findNextPage(cursorTitle, cursorId, pageable);

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

        /*
        * TODO : Long != 비교 버그
        * Long은 객체.
        * 객체를 == 또는 != 로 비교하면 값이 아니라 메모리 주소(참조) 비교가 됨
        * 왜 테스트할 때 동작했는가?
        * JVM이 Long 객체를 -128 ~ 127 범위에서는 미리 만들어놓고 캐싱해서 재사용함
        * (JVM 뜰 때 Long 객체를 해당 범위만큼 미리 256개 생성해서 메모리에 올림)
        * 그래서 userId가 그 범위 안에 있으면 Long a = 100L, Long b= 100L이 우연히 같은 객체를 가리키게 되고
        * != 로 비교해도 false가 나와서 코드가 정상 동작하는 것처럼 보임
        */

         if(!board.getUserId().equals(user.getUserId())){
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