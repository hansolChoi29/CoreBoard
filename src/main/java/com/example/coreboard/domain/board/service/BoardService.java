package com.example.coreboard.domain.board.service;


import com.example.coreboard.domain.board.dto.BoardRequest;
import com.example.coreboard.domain.board.dto.BoardApiResponse;
import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.repository.BoardRepository;
import org.springframework.stereotype.Service;

@Service
public class BoardService {
    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository){
        this.boardRepository=boardRepository;
    }

    // 보드 생성
    public BoardApiResponse createBoard(BoardRequest boardRequestDto){
        // 보드 저장
        Board board= Board.createBoard(
                boardRequestDto.getBoardTitle(),
                boardRequestDto.getBoardContents()
                );
        boardRepository.save(board);
        return new BoardApiResponse(board.getBoardTitle(),"게시글이 성공적으로 생성되었습니다.");
    }
}
