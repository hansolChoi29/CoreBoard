package com.example.coreboard.domain.post.controller;


import com.example.coreboard.domain.common.response.ApiResponse;
import com.example.coreboard.domain.common.response.OffsetPageResponse;
import com.example.coreboard.domain.common.validation.PostValidation;
import com.example.coreboard.domain.post.dto.command.CreatePostCommand;
import com.example.coreboard.domain.post.dto.request.CreatePostRequest;
import com.example.coreboard.domain.post.dto.response.CreatePostResponse;
import com.example.coreboard.domain.post.dto.response.PostSummaryResponse;
import com.example.coreboard.domain.post.dto.result.CreatePostResult;
import com.example.coreboard.domain.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Post", description = "게시판에 종속된 게시글 관련 API")
@RestController
@RequestMapping("/boards/{boardId}/posts")
public class BoardPostController {
    private final PostService postService;

    public BoardPostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "게시글 생성", description = "로그인 후 title, content 넣고 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<CreatePostResponse>> create(
            @PathVariable Long boardId,
            @RequestBody CreatePostRequest request,
            @RequestAttribute("username") String username
    ) {
        PostValidation.validateForCreate(request);

        CreatePostCommand post = new CreatePostCommand(
                boardId,
                request.title(),
                request.content(),
                request.contentFormat(),
                request.attachmentIds());

        CreatePostResult out = postService.create(post, username);

        CreatePostResponse response = new CreatePostResponse(out.id());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "게시글이 성공적으로 생성되었습니다."));
    }

    @Operation(summary = "게시글 전체 조회", description = "오프셋 기반 페이지네이션. page: 0부터 시작, sort: asc/desc")
    @GetMapping
    public ResponseEntity<ApiResponse<OffsetPageResponse<PostSummaryResponse>>> getAll(
            @PathVariable Long boardId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "desc") String sort
    ) {
        PostValidation.validateSortDirection(sort);
        PostValidation.validatePageSize(size);
        OffsetPageResponse<PostSummaryResponse> response = postService.getAll(
                boardId,
                page,
                size,
                sort
        );

        return ResponseEntity.ok(ApiResponse.ok(response, "게시글 전체 조회!"));
    }
}
