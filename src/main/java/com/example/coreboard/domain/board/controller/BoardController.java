package com.example.coreboard.domain.board.controller;


import com.example.coreboard.domain.board.dto.*;
import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.service.BoardService;
import com.example.coreboard.domain.common.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<ApiResponse<BoardCreateResponse>> createBoard(
            @RequestBody BoardRequest boardRequestDto,      // JSON 데이터를 boardRequestDto로 받겠다.
            @RequestAttribute("username") String username   // 인터셉터의 username 이용
    ) {
        BoardCreateResponse responseDto = boardService.createBoard(boardRequestDto, username); // title과 contents,
        // uesrname 같이 응답하기 위함
        return ResponseEntity.ok(ApiResponse.ok(responseDto, "게시글이 성공적으로 생성되었습니다."));
    }

    // 보드 단건조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardGetOneResponse>> getOneBoard(
            @PathVariable Long id                                        // 단건 조회라서 id 받게 함
    ) {
        BoardGetOneResponse responseDto = boardService.findOneBoard(id); // 유저id와 게시글id findOneBoard 실행하여
        // 반환된 값 변수에 넣음
        return ResponseEntity.ok(ApiResponse.ok(responseDto, "게시글 단건 조회!"));
    }

    // 1) 보드 전체 조회 - PageableRequset
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Board>>> getAllBoard(
            // RequestParam : ?paeg=0&size=10 바인딩 해주는 어노테이션
            @RequestParam(defaultValue = "0") int page, // 클라이언트 요청 page
            @RequestParam(defaultValue = "10") int size // 클라이언트 요청 size
    ) {
        // pageable = page와 size, 정렬(내림차순) 규칙이 설정된 createdDate를 객체(Pageable)를 담음
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Board> result = boardService.findAllBoard(pageable);
        return ResponseEntity.ok(ApiResponse.ok(result, "게시글 전체 조회!"));
    }

    // 2) 보드 전체 조회 - Cursor


    // 보드 수정
    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardUpdateResponse>> updateBoard(
            @RequestBody BoardRequest boardRequestDto,
            @RequestAttribute("username") String username,
            @PathVariable Long id
    ) {
        BoardUpdateResponse responseDto = boardService.updateBoard(boardRequestDto, username, id);
        return ResponseEntity.ok(ApiResponse.ok(responseDto, "게시글 수정 완료!"));
    }

    // 보드 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardDeleteResponse>> deleteBoard(
            @RequestAttribute("username") String username,
            @PathVariable Long id
    ) {
        BoardDeleteResponse responseDto = boardService.deleteBoard(username, id);
        return ResponseEntity.ok(ApiResponse.ok(responseDto, "게시글 삭제완료!"));
    }
}