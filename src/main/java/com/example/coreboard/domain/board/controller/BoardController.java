package com.example.coreboard.domain.board.controller;


import com.example.coreboard.domain.board.dto.*;
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
        return ResponseEntity.ok(ApiResponse.ok(responseDto, "게시글 단건 조회!"));
    }

    //보드 전체 조회 - 페이지네이션 공부하기;;
    @GetMapping
    public ResponseEntity<ApiResponse<PageResultResponse<BoardResponse>>> getAllBoard(
            @RequestAttribute("username") String username,
            @RequestParam(defaultValue = "1") int page, // 클라이언트 요청 page
            @RequestParam(defaultValue="10") int size // 클라이언트 요청 size
    ) {
        // 서비스 호충해서 페이지네이션 처리 및 Board -> DTO 변환
        PageResultResponse<BoardResponse> responseDto = boardService.findAllBoard(username, page, size);
        return ResponseEntity.ok(ApiResponse.ok(responseDto, "게시글 전체 조회!"));
    }

    // 보드 수정
    @PostMapping("/{boardId}")
    public ResponseEntity<ApiResponse<BoardResponse>> updateBoard(
            @RequestBody BoardRequest boardRequestDto,
            @RequestAttribute("username") String username,
            @PathVariable Long boardId
    ) {
        BoardResponse responseDto = boardService.updateBoard(boardRequestDto, username, boardId);
        return ResponseEntity.ok(ApiResponse.ok(responseDto, "게시글 수정 완료!"));
    }

    // 보드 삭제
    @DeleteMapping("/{boardId}")
    public ResponseEntity<ApiResponse<BoardDeleteResponse>> deleteBoard(
            @RequestAttribute("username") String username,
            @PathVariable Long boardId
    ) {
        BoardDeleteResponse responseDto = boardService.deleteBoard(username, boardId);
        return ResponseEntity.ok(ApiResponse.ok(responseDto, "게시글 삭제완료!"));
    }
}