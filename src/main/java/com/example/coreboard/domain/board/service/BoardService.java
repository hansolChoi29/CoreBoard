package com.example.coreboard.domain.board.service;


import com.example.coreboard.domain.board.dto.BoardRequest;
import com.example.coreboard.domain.board.dto.BoardApiResponse;
import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import org.springframework.stereotype.Service;

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
}
