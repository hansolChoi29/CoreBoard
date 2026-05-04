package com.example.coreboard.domain.post.controller;

import com.example.coreboard.domain.post.dto.command.CreatePostCommand;
import com.example.coreboard.domain.post.dto.command.DeletePostCommand;
import com.example.coreboard.domain.post.dto.command.GetOnePostCommand;
import com.example.coreboard.domain.post.dto.command.UpdatePostCommand;
import com.example.coreboard.domain.post.dto.request.CreatePostRequest;
import com.example.coreboard.domain.post.dto.request.UpdatePostRequest;
import com.example.coreboard.domain.post.dto.response.CreatePostResponse;
import com.example.coreboard.domain.post.dto.response.GetOnePostResponse;
import com.example.coreboard.domain.post.dto.response.PostSummaryResponse;
import com.example.coreboard.domain.post.dto.response.UpdatePostResponse;
import com.example.coreboard.domain.post.dto.result.CreatePostResult;
import com.example.coreboard.domain.post.dto.result.GetOnePostResult;
import com.example.coreboard.domain.post.dto.result.UpdatePostResult;
import com.example.coreboard.domain.post.service.PostService;
import com.example.coreboard.domain.common.validation.PostValidation;
import com.example.coreboard.domain.common.response.ApiResponse;
import com.example.coreboard.domain.common.response.CursorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Post", description = "게시글 관련 API")
@RestController
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "게시글 생성", description = "로그인 후 title, content 넣고 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<CreatePostResponse>> create(
            @RequestBody CreatePostRequest request,
            @RequestAttribute("username") String username
    ) {
        PostValidation.validateForCreate(request);

        CreatePostCommand post = new CreatePostCommand(
                request.boardId(),
                request.title(),
                request.content(),
                request.contentFormat());

        CreatePostResult out = postService.create(post, username);

        CreatePostResponse response = new CreatePostResponse(out.id());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "게시글이 성공적으로 생성되었습니다."));
    }

    @Operation(summary = "게시글 단건 조회", description = "로그인 없이도 id로 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GetOnePostResponse>> getOne(
            @PathVariable("id") Long id
    ) {
        GetOnePostCommand board = new GetOnePostCommand(id);

        GetOnePostResult out = postService.getOne(board);

        GetOnePostResponse response = new GetOnePostResponse(
                out.id(),
                out.userId(),
                out.title(),
                out.content(),
                out.createdDate(),
                out.lastModifiedDate()
        );

        return ResponseEntity.ok(ApiResponse.ok(response, "게시글 단건 조회!"));
    }

    @Operation(summary = "게시글 전체 조회", description = "커서 기반 페이지네이션, sort: asc/desc")
    @GetMapping
    public ResponseEntity<ApiResponse<CursorResponse<PostSummaryResponse>>> getAll(
            @RequestParam(name = "cursorTitle", required = false) String cursorTitle,
            @RequestParam(name = "cursorId", required = false) Long cursorId,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "asc") String sort
    ) {
        PostValidation.validateSortDirection(sort);
        PostValidation.validatePageSize(size);
        CursorResponse<PostSummaryResponse> response = postService.getAll(
                cursorTitle,
                cursorId,
                size,
                sort);

        return ResponseEntity.ok(ApiResponse.ok(response, "게시글 전체 조회!"));
    }

    @Operation(summary = "게시글 수정", description = "본인 게시글만 수정 가능")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UpdatePostResponse>> update(
            @RequestBody UpdatePostRequest request,
            @RequestAttribute("username") String username,
            @PathVariable("id") Long id
    ) {
        PostValidation.validateForUpdate(request);

        UpdatePostCommand board = new UpdatePostCommand(
                id,
                username,
                request.title(),
                request.content(),
                request.contentFormat()
        );

        UpdatePostResult out = postService.update(board);

        UpdatePostResponse response = new UpdatePostResponse(
                out.id(),
                out.createdAt(),
                out.updatedAt());

        return ResponseEntity.ok(ApiResponse.ok(response, "게시글이 성공적으로 수정되었습니다."));
    }

    @Operation(summary = "게시글 삭제", description = "본인 게시글만 삭제 가능")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestAttribute("username") String username,
            @PathVariable("id") Long id
    ) {
        // CQRS는 읽기 작업과 쓰기 작업을 별도의 데이터 모델로 분리하는 디자인 패턴이다
        // 각 모델을 독립적으로 최적화할 수 있고, 성능·확장성·보안을 향상시킬 수 있다
        // 즉, command가 필요한 이유는 게시판 삭제 명령을 구성하는 데이터이기 때문이다
        DeletePostCommand command = new DeletePostCommand(id, username);
        postService.delete(command);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "게시글 검색")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<CursorResponse<PostSummaryResponse>>> search(
            @RequestParam("keyword") String keyword
    ) {
        CursorResponse<PostSummaryResponse> response = postService.search(keyword);

        return ResponseEntity.ok(ApiResponse.ok(response, "게시글 검색 성공!"));
    }
}