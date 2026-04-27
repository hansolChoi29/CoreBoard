package com.example.coreboard.domain.post.controller;

import com.example.coreboard.domain.post.dto.*;
import com.example.coreboard.domain.post.dto.command.PostCreateCommand;
import com.example.coreboard.domain.post.dto.command.PostGetOneCommand;
import com.example.coreboard.domain.post.dto.command.PostUpdateCommand;
import com.example.coreboard.domain.post.dto.request.PostCreateRequest;
import com.example.coreboard.domain.post.dto.request.PostUpdateRequest;
import com.example.coreboard.domain.post.dto.response.PostCreateResponse;
import com.example.coreboard.domain.post.dto.response.PostGetOneResponse;
import com.example.coreboard.domain.post.dto.response.PostSummaryKeysetResponse;
import com.example.coreboard.domain.post.dto.response.PostUpdateResponse;
import com.example.coreboard.domain.post.service.PostService;
import com.example.coreboard.domain.common.validation.PostValidation;
import com.example.coreboard.domain.common.response.ApiResponse;
import com.example.coreboard.domain.common.response.CursorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Board", description = "게시글 관련 API")
@RestController
@RequestMapping("/board")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "게시글 생성", description = "로그인 후 title, content 넣고 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<PostCreateResponse>> create(
            @RequestBody PostCreateRequest boardRequestDto,
            @RequestAttribute("username") String username
    ) {
        PostValidation.createValidation(boardRequestDto);

        PostCreateCommand board = new PostCreateCommand(
                boardRequestDto.boardId(),
                boardRequestDto.title(),
                boardRequestDto.content(),
                boardRequestDto.contentFormat());

        PostCreateDto out = postService.create(board, username);

        PostCreateResponse response = new PostCreateResponse(
                out.getId(),
                out.getUserId(),
                out.getTitle(),
                out.getContent(),
                out.getCreatedDate());

        return ResponseEntity.ok(ApiResponse.ok(response, "게시글이 성공적으로 생성되었습니다."));
    }

    @Operation(summary = "게시글 단건 조회", description = "로그인 없이도 id로 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostGetOneResponse>> getOne(
            @PathVariable("id") Long id
    ) {
        PostGetOneCommand board = new PostGetOneCommand(id);

        PostGetOneDto out = postService.findOne(board);

        PostGetOneResponse response = new PostGetOneResponse(
                out.getId(),
                out.getUserId(),
                out.getTitle(),
                out.getContent(),
                out.getCreatedDate(),
                out.getLastModifiedDate()
        );

        return ResponseEntity.ok(ApiResponse.ok(response, "게시글 단건 조회!"));
    }

    @Operation(summary = "게시글 전체 조회", description = "커서 기반 페이지네이션, sort: asc/desc")
    @GetMapping
    public ResponseEntity<ApiResponse<CursorResponse<PostSummaryKeysetResponse>>> getAll(
            @RequestParam(name = "cursorTitle", required = false) String cursorTitle,
            @RequestParam(name = "cursorId", required = false) Long cursorId,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "asc") String sort
    ) {
        PostValidation.sortDirection(sort);

        PostValidation.pageableValication(size);

        CursorResponse<PostSummaryKeysetResponse> response = postService.findAll(
                cursorTitle,
                cursorId,
                size,
                sort
        );

        return ResponseEntity.ok(ApiResponse.ok(response, "게시글 전체 조회!"));
    }

    @Operation(summary = "게시글 수정", description = "본인 게시글만 수정 가능")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostUpdateResponse>> update(
            @RequestBody PostUpdateRequest updateRequestDto,
            @RequestAttribute("username") String username,
            @PathVariable("id") Long id
    ) {
        PostValidation.updateValidation(updateRequestDto);

        PostUpdateCommand board = new PostUpdateCommand(
                username,
                id,
                updateRequestDto.title(),
                updateRequestDto.content(),
                updateRequestDto.contentFormat()
        );

        PostUpdatedDto out = postService.update(board);

        PostUpdateResponse response = new PostUpdateResponse(out.getId());

        return ResponseEntity.ok(ApiResponse.ok(response, "게시글이 성공적으로 수정되었습니다."));
    }

    @Operation(summary = "게시글 삭제", description = "본인 게시글만 삭제 가능")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestAttribute("username") String username,
            @PathVariable("id") Long id
    ) {
        postService.delete(username, id);

        return ResponseEntity.ok(ApiResponse.ok(null, "게시글이 성공적으로 삭제되었습니다."));
    }

    @Operation(summary = "게시글 검색")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<CursorResponse<PostSummaryKeysetResponse>>> search(
            @RequestParam("keyword") String keyword
    ) {
        CursorResponse<PostSummaryKeysetResponse> response = postService.search(keyword);
        return ResponseEntity.ok(ApiResponse.ok(response, "게시글 검색 성공!"));
    }
}