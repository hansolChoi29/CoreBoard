package com.example.coreboard.domain.board.controller;


import com.example.coreboard.domain.board.dto.*;
import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.board.service.BoardService;
import com.example.coreboard.domain.common.validation.BoardValidation;
import com.example.coreboard.domain.common.response.ApiResponse;
import com.example.coreboard.domain.common.response.PageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/board")
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    // 보드 생성
    @PostMapping
    public ResponseEntity<ApiResponse<BoardCreateResponse>> create(
            @RequestBody BoardCreateRequest boardRequestDto,      // JSON 데이터를 boardRequestDto로 받겠다.
            @RequestAttribute("username") String username   // 인터셉터의 username 이용
    ) {
        BoardValidation.createValidation(boardRequestDto); // 유효성 검사
        Board board = boardService.create(boardRequestDto, username); // 저장된 Board 객체가 반환되어 board로 들어감
        BoardCreateResponse response = new BoardCreateResponse( // DB에 저장된 엔티티를 응답용으로 변환하여 return 세팅
                board.getId(), board.getUserId(), board.getTitle(), board.getContent(), board.getCreatedDate()
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "게시글이 성공적으로 생성되었습니다."));
    }

    // 보드 단건조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardGetOneResponse>> getOne(
            @PathVariable Long id                                        // 단건 조회라서 id 받게 함
    ) {
        Board board = boardService.findOne(id); // id 추출하여 board 반환
        BoardGetOneResponse response = new BoardGetOneResponse( // 응답용 세팅
                board.getId(), board.getUserId(), board.getTitle(), board.getContent(), board.getCreatedDate(),
                board.getLastModifiedDate()
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "게시글 단건 조회!"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BoardSummaryResponse>>> getAll(
            // RequestParam : ?paeg=0&size=10 바인딩 해주는 어노테이션
            @RequestParam(defaultValue = "0") int page, // 클라이언트 요청 page
            @RequestParam(defaultValue = "10") int size, // 클라이언트 요청 size
            @RequestParam(defaultValue = "asc") String sort
    ) {
        // equalsIgnoreCase : 문자열 비교 (대소문자를 구분하지 않고 비교)
        BoardValidation.sortDirection(sort);
        BoardValidation.pageableValication(page, size);
        PageResponse<BoardSummaryResponse> response = boardService.findAll(page, size, sort);
        return ResponseEntity.ok(ApiResponse.ok(response, "게시글 전체 조회!"));
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
        Board board = boardService.update(updateRequestDto, username, id);
        BoardUpdateResponse responseDto = new BoardUpdateResponse(
                board.getId(), board.getUserId(), board.getTitle(), board.getContent(), board.getLastModifiedDate()
        );
        return ResponseEntity.ok(ApiResponse.ok(responseDto, "게시글 수정 완료!"));
    }

    // 보드 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestAttribute("username") String username,
            @PathVariable Long id
    ) {
         boardService.delete(username, id);
         // 자원을 없앴으니 응답 바디에 실어줄 자원 데이터 자체가 존재하지 않음
        return ResponseEntity.ok(ApiResponse.ok(null,"게시글 삭제완료!"));
    }
}