package com.example.coreboard.domain.board.service;


import com.example.coreboard.domain.board.dto.BoardRequest;
import com.example.coreboard.domain.board.dto.BoardApiResponse;
import com.example.coreboard.domain.board.dto.BoardResponse;
import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;
import org.springframework.stereotype.Service;

import static com.example.coreboard.domain.common.exception.auth.AuthErrorCode.*;
import static com.example.coreboard.domain.common.exception.board.BoardErrorCode.*;

@Service
public class BoardService {
    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    // 보드 생성
    public BoardApiResponse createBoard(
            BoardRequest boardRequestDto,
            String username // 인터셉터에서 가로채 검증을 끝내고 반환된 username을 컨트롤러에서 받아와 board에 저장하기
    ) {

        // 보드 저장할 것들 세팅
        Board board = Board.createBoard(
                boardRequestDto.getBoardTitle(),
                boardRequestDto.getBoardContents(),
                username
        );
        boardRepository.save(board); // 저장
        return new BoardApiResponse(board.getBoardTitle(), "게시글이 성공적으로 생성되었습니다.");
    }

    // 보드 단건 조회
    public BoardResponse findOneBoard(
            String username,
            Long boardId
    ) {
        Board board = boardRepository.findById(boardId) // id 추출하는 메서드 이용해서
                .orElseThrow(() -> new BoardErrorException(POST_NOT_FOUND)); // 값이 있으면 반환 없으면 에러 던짐

        if (!board.getUsername().equals(username)) { // 권한 체크
            throw new AuthErrorException(FORBIDDEN);
        }

        // 트러블 - board만 넣었더니 500 에러: 단건 조회용, 타이틀과 본문 응답 반환
        return new BoardResponse(
                board.getBoardTitle(),
                board.getBoardContents()
        );
    }
}
