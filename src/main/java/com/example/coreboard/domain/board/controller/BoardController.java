package com.example.coreboard.domain.board.controller;


import com.example.coreboard.domain.board.dto.BoardRequest;
import com.example.coreboard.domain.board.dto.BoardApiResponse;
import com.example.coreboard.domain.board.service.BoardService;
import com.example.coreboard.domain.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/board")
public class BoardController {
    private final BoardService boardService;
    public BoardController(BoardService boardService){
        this.boardService=boardService;
    }

    // 보드 생성
    @PostMapping
    public ResponseEntity<ApiResponse<BoardApiResponse>> createBoard(
            @RequestBody BoardRequest boardRequestDto // JSON 데이터를 boardRequestDto로 받겠다.
    ){
        BoardApiResponse responseDto= boardService.createBoard(boardRequestDto);
        return ResponseEntity.ok(ApiResponse.ok(responseDto, "게시글이 성공적으로 생성되었습니다."));
    }
}
