package com.example.coreboard.domain.post.controller;

import com.example.coreboard.domain.common.response.OffsetPageResponse;
import com.example.coreboard.domain.post.dto.command.DeletePostCommand;
import com.example.coreboard.domain.post.dto.command.GetOnePostCommand;
import com.example.coreboard.domain.post.dto.command.UpdatePostCommand;
import com.example.coreboard.domain.post.dto.request.UpdatePostRequest;
import com.example.coreboard.domain.post.dto.response.GetOnePostResponse;
import com.example.coreboard.domain.post.dto.response.PostSummaryResponse;
import com.example.coreboard.domain.post.dto.response.UpdatePostResponse;
import com.example.coreboard.domain.post.dto.result.GetOnePostResult;
import com.example.coreboard.domain.post.dto.result.UpdatePostResult;
import com.example.coreboard.domain.post.service.PostService;
import com.example.coreboard.domain.common.validation.PostValidation;
import com.example.coreboard.domain.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Post", description = "게시판에 종속되지 않은 게시글 관련 API")
@RestController
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation()
    @GetMapping
    public ResponseEntity<ApiResponse<OffsetPageResponse<PostSummaryResponse>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "desc") String sort
    ) {
        PostValidation.validateSortDirection(sort);
        PostValidation.validatePageSize(size);
        OffsetPageResponse<PostSummaryResponse> response = postService.getAll(
                page,
                size,
                sort
        );
        
        return ResponseEntity.ok(ApiResponse.ok(response, "게시글 전체조회!"));
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
                out.lastModifiedDate(),
                out.comments(),
                out.attachments()
        );

        return ResponseEntity.ok(ApiResponse.ok(response, "게시글 단건 조회!"));
    }

    @Operation(summary = "게시글 수정", description = "작성자 또는 ADMIN만 게시글을 수정할 수 있습니다.")
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
                request.contentFormat(),
                request.keepAttachmentIds(),
                request.newAttachmentIds()
        );

        UpdatePostResult out = postService.update(board);

        UpdatePostResponse response = new UpdatePostResponse(
                out.id(),
                out.createdAt(),
                out.updatedAt());

        return ResponseEntity.ok(ApiResponse.ok(response, "게시글이 성공적으로 수정되었습니다."));
    }

    @Operation(summary = "게시글 삭제", description = "작성자 또는 ADMIN만 게시글을 삭제할 수 있습니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestAttribute("username") String username,
            @PathVariable("id") Long id
    ) {
        DeletePostCommand command = new DeletePostCommand(id, username);
        postService.delete(command);

        return ResponseEntity.noContent().build();
    }
}