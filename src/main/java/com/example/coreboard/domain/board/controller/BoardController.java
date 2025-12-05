package com.example.coreboard.domain.board.controller;


import com.example.coreboard.domain.board.dto.*;
import com.example.coreboard.domain.board.dto.command.BoardCreateCommand;
import com.example.coreboard.domain.board.dto.command.BoardGetOneCommand;
import com.example.coreboard.domain.board.dto.command.BoardUpdateCommand;
import com.example.coreboard.domain.board.dto.request.BoardCreateRequest;
import com.example.coreboard.domain.board.dto.request.BoardUpdateRequest;
import com.example.coreboard.domain.board.dto.response.BoardCreateResponse;
import com.example.coreboard.domain.board.dto.response.BoardGetOneResponse;
import com.example.coreboard.domain.board.dto.response.BoardSummaryResponse;
import com.example.coreboard.domain.board.dto.response.BoardUpdateResponse;
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

    @PostMapping
    public ResponseEntity<ApiResponse<BoardCreateResponse>> create(
            @RequestBody BoardCreateRequest boardRequestDto,
            @RequestAttribute("username") String username
    ) {
        BoardValidation.createValidation(boardRequestDto);

        BoardCreateCommand board = new BoardCreateCommand(boardRequestDto.getTitle(), boardRequestDto.getContent());

        BoardCreateDto out = boardService.create(board, username);

        BoardCreateResponse response = new BoardCreateResponse(
                out.getId(), out.getUserId(), out.getTitle(), out.getContent(), out.getCreatedDate()
        );

        return ResponseEntity.ok(ApiResponse.ok(response, "게시글이 성공적으로 생성되었습니다."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardGetOneResponse>> getOne(
            @PathVariable Long id
    ) {
        BoardGetOneCommand board = new BoardGetOneCommand(id);

        BoardGetOneDto out = boardService.findOne(board);

        BoardGetOneResponse response = new BoardGetOneResponse(
                out.getId(), out.getUserId(), out.getTitle(), out.getContent(), out.getCreatedDate(),
                out.getLastModifiedDate()
        );

        return ResponseEntity.ok(ApiResponse.ok(response, "게시글 단건 조회!"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BoardSummaryResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort
    ) {
        BoardValidation.sortDirection(sort);

        BoardValidation.pageableValication(page, size);

        PageResponse<BoardSummaryResponse> response = boardService.findAll(page, size, sort);

        return ResponseEntity.ok(ApiResponse.ok(response, "게시글 전체 조회!"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardUpdateResponse>> update(
            @RequestBody BoardUpdateRequest updateRequestDto,
            @RequestAttribute("username") String username,
            @PathVariable Long id
    ) {
        BoardValidation.updateValidation(updateRequestDto);

        BoardUpdateCommand board = new BoardUpdateCommand(username, id, updateRequestDto.getTitle(),
                updateRequestDto.getContent());

        BoardUpdatedDto out = boardService.update(board);

        BoardUpdateResponse response = new BoardUpdateResponse(out.getId());

        return ResponseEntity.ok(ApiResponse.ok(response, "게시글 수정 완료!"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestAttribute("username") String username,
            @PathVariable Long id
    ) {
        boardService.delete(username, id);
        return ResponseEntity.ok(ApiResponse.ok(null, "게시글 삭제완료!"));
    }
}