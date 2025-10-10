package com.example.coreboard.domain.board.controller;


import com.example.coreboard.domain.board.dto.BoardRequest;
import com.example.coreboard.domain.board.dto.BoardApiResponse;
import com.example.coreboard.domain.board.dto.BoardResponse;
import com.example.coreboard.domain.board.service.BoardService;
import com.example.coreboard.domain.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/board")
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    // 보드 생성
    @PostMapping
    public ResponseEntity<ApiResponse<BoardApiResponse>> createBoard(
            @RequestBody BoardRequest boardRequestDto,      // JSON 데이터를 boardRequestDto로 받겠다.
            @RequestAttribute("username") String username   // 인터셉터의 username 이용
    ) {
        BoardApiResponse responseDto = boardService.createBoard(boardRequestDto, username); // title과 contents, 
        // uesrname 같이 응답하기 위함
        return ResponseEntity.ok(ApiResponse.ok(responseDto, "게시글이 성공적으로 생성되었습니다."));
    }

    // 보드 단건조회
    @GetMapping("/{boardId}")
    public ResponseEntity<ApiResponse<BoardResponse>> getOneBoard(
            @RequestAttribute("username") String username, // 유저 id 뽑아와서 권한체크
            @PathVariable Long boardId                      // 단건 조회라서 id 받게 함
    ) {
        BoardResponse responseDto = boardService.findOneBoard(username, boardId);    // 유저id와 게시글id findOneBoard 실행하여
                                                                                     // 반환된 값 변수에 넣음
        return ResponseEntity.ok(ApiResponse.ok(responseDto, "게시글 조회!"));
    }
}