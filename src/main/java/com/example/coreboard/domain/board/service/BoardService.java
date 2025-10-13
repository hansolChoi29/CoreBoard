package com.example.coreboard.domain.board.service;


import com.example.coreboard.domain.board.dto.*;
import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.coreboard.domain.common.exception.auth.AuthErrorCode.*;
import static com.example.coreboard.domain.common.exception.board.BoardErrorCode.*;

@Service
public class BoardService {
    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    // 보드 생성
    public BoardCreateResponse createBoard(
            BoardRequest boardRequestDto,
            String username // 인터셉터에서 가로채 검증을 끝내고 반환된 username을 컨트롤러에서 받아와 board에 저장하기
    ) {

        // 보드 저장할 것들 세팅
        Board board = Board.create(
                username,
                boardRequestDto.getBoardTitle(),
                boardRequestDto.getBoardContents()
        );
        boardRepository.save(board); // 저장
        return new BoardCreateResponse(board.getId(), username, board.getBoardTitle(), board.getBoardContents(),
                board.getCreatedDate());
    }

    // 보드 단건 조회
    public BoardGetOneResponse findOneBoard(
            String username,
            Long id
    ) {
        Board board = boardRepository.findById(id) // id 추출하는 메서드 이용해서
                .orElseThrow(() -> new BoardErrorException(POST_NOT_FOUND)); // 값이 있으면 반환 없으면 에러 던짐

        if (!board.getUsername().equals(username)) { // 권한 체크
            throw new AuthErrorException(FORBIDDEN);
        }

        // 트러블 - board만 넣었더니 500 에러: 단건 조회용, 타이틀과 본문 응답 반환
        return new BoardGetOneResponse(
                board.getId(),
                username,
                board.getBoardTitle(),
                board.getBoardContents(),
                board.getCreatedDate(),
                board.getLastModifiedDate()
        );
    }

    // 보드 전체 조회
    public Page<Board> findAllBoard(Pageable pageable
    ) {
        return boardRepository.findAll(pageable);
    }

    // 보드 수정 트러블 - 성공응답 나오지만, 조회 시 수정이 안되는 이슈 발생(Transactional)
    @Transactional
    public BoardUpdateResponse updateBoard(
            BoardRequest boardRequestDto,
            String username,
            Long id
    ) {
        Board board = boardRepository.findById(id) // id 추출하는 메서드 이용해서
                .orElseThrow(() -> new BoardErrorException(POST_NOT_FOUND)); // 값이 있으면 반환 없으면 에러 던짐
        if (!board.getUsername().equals(username)) { // 권한 체크
            throw new AuthErrorException(FORBIDDEN);
        }

        // 저장
        board.update(
                boardRequestDto.getBoardTitle(),
                boardRequestDto.getBoardContents()
        );
        return new BoardUpdateResponse(
                board.getId(),
                username,
                board.getBoardTitle(),
                board.getBoardContents(),
                board.getLastModifiedDate()
        );
    }

    // 보드 삭제
    public BoardDeleteResponse deleteBoard(
            String username,
            Long id
    ) {
        Board board = boardRepository.findById(id) // id 추출하는 메서드 이용해서
                .orElseThrow(() -> new BoardErrorException(POST_NOT_FOUND)); // 값이 있으면 반환 없으면 에러 던짐

        if (!board.getUsername().equals(username)) { // 권한 체크
            throw new AuthErrorException(FORBIDDEN);
        }

        boardRepository.delete(board); // 스프링에서 제공되는 삭제 메서드

        return new BoardDeleteResponse(
                board
        );
    }
}
