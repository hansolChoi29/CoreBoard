package com.example.coreboard.domain.board.controller;


import com.example.coreboard.domain.board.dto.*;
import com.example.coreboard.domain.board.service.BoardService;
import com.example.coreboard.domain.board.validation.BoardValidation;
import com.example.coreboard.domain.common.exception.auth.AuthErrorCode;
import com.example.coreboard.domain.common.exception.auth.AuthErrorException;
import com.example.coreboard.domain.common.exception.board.BoardErrorCode;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;
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
    public ResponseEntity<ApiResponse<BoardCreateResponse>> createBoard(
            @RequestBody BoardCreateRequest boardRequestDto,      // JSON 데이터를 boardRequestDto로 받겠다.
            @RequestAttribute(name = "username", required = false) String username   // 인터셉터의 username 이용
    ) {
        if (username == null) {
            throw new AuthErrorException(AuthErrorCode.UNAUTHORIZED); // 401
        }
        BoardValidation.createValidation(boardRequestDto);
        BoardCreateResponse responseDto = boardService.create(boardRequestDto, username); // title과 contents,
        // uesrname 같이 응답하기 위함
        return ResponseEntity.ok(ApiResponse.ok(responseDto, "게시글이 성공적으로 생성되었습니다."));
    }

    // 보드 단건조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardGetOneResponse>> getOne(
            @PathVariable Long id                                        // 단건 조회라서 id 받게 함
    ) {
        BoardGetOneResponse responseDto = boardService.findOne(id); // 유저id와 게시글id findOneBoard 실행하여
        // 반환된 값 변수에 넣음
        return ResponseEntity.ok(ApiResponse.ok(responseDto, "게시글 단건 조회!"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BoardSummaryResponse>>> getAll(
            // RequestParam : ?paeg=0&size=10 바인딩 해주는 어노테이션
            @RequestParam(defaultValue = "0") int page, // 클라이언트 요청 page
            @RequestParam(defaultValue = "10") int size, // 클라이언트 요청 size
            @RequestParam(defaultValue = "asc") String sort
    ) {
        // equalsIgnoreCase : 문자열 비교 (대소문자를 구분하지 않고 비교)
        if (!sort.equalsIgnoreCase("asc") && !sort.equalsIgnoreCase("desc")) {
            throw new BoardErrorException(BoardErrorCode.SORT_DIRECTION_INVALID);
        }

        BoardValidation.pageableValication(page, size);
        return ResponseEntity.ok(boardService.findAll(page, size, sort));
    }

    // 2) 보드 전체 조회 - Cursor


    // 보드 수정 - 멱등의 개념
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardUpdateResponse>> update(
            @RequestBody BoardUpdateRequest updateRequestDto,
            @RequestAttribute("username") String username,
            @PathVariable Long id
    ) {
        BoardValidation.updateValidation(updateRequestDto);  // 유효성 검사
        BoardUpdateResponse responseDto = boardService.update(updateRequestDto, username, id);
        return ResponseEntity.ok(ApiResponse.ok(responseDto, "게시글 수정 완료!"));
    }

    // 보드 삭제 - TODO: 멱등하지 않다. (개선필요)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardDeleteResponse>> delete(
            @RequestAttribute("username") String username,
            @PathVariable Long id
    ) {
        BoardDeleteResponse responseDto = boardService.delete(username, id);
        return ResponseEntity.ok(ApiResponse.ok(responseDto, "게시글 삭제완료!"));
    }
}