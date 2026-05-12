package com.example.coreboard.domain.comment.controller;

import com.example.coreboard.domain.comment.dto.command.CommentCommand;
import com.example.coreboard.domain.comment.dto.query.GetCommentQuery;
import com.example.coreboard.domain.comment.dto.request.CommentRequest;
import com.example.coreboard.domain.comment.dto.response.CommentResponse;
import com.example.coreboard.domain.comment.dto.response.GetAllCommentResponse;
import com.example.coreboard.domain.common.response.SliceResponse;
import com.example.coreboard.domain.comment.dto.result.CommentResult;
import com.example.coreboard.domain.comment.service.CommentService;
import com.example.coreboard.domain.common.response.ApiResponse;
import com.example.coreboard.domain.common.validation.CommentValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Comment", description = "댓글 관련 API")
@RestController
@RequestMapping("/posts/{postId}/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "댓글 작성", description = "로그인 사용자가 게시글에 댓글을 작성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> create(
            @PathVariable Long postId,
            @RequestAttribute("username") String username,
            @RequestBody CommentRequest request
    ) {
        CommentValidation.validate(request);
        CommentCommand command = new CommentCommand(request.content());
        CommentResult result = commentService.create(postId, username, command);
        CommentResponse response = new CommentResponse(result.id());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "댓글이 성공적으로 작성되었습니다."));
    }

    @Operation(summary = "댓글 목록 조회", description = "게시글의 댓글 목록을 Slice 방식으로 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<SliceResponse<GetAllCommentResponse>>> getAll(
            @PathVariable Long postId,
            @RequestParam(name = "page") int page,
            @RequestParam(name = "size") int size
    ) {
        GetCommentQuery query = new GetCommentQuery(
                postId,
                page,
                size
        );
        SliceResponse<GetAllCommentResponse> response = commentService.getAll(query);

        return ResponseEntity.ok(ApiResponse.ok(response, "성공적으로 불러왔습니다."));
    }

    @Operation(summary = "댓글 수정", description = "댓글 작성자만 댓글을 수정할 수 있습니다.")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CommentResponse>> update(
            @RequestAttribute("username") String username,
            @PathVariable("postId") Long postId,
            @PathVariable("id") Long id,
            @RequestBody CommentRequest request
    ) {
        CommentValidation.validate(request);
        CommentCommand command = new CommentCommand(request.content());
        CommentResult result = commentService.update(
                username,
                postId,
                id,
                command
        );
        CommentResponse response = new CommentResponse(result.id());
        return ResponseEntity.ok(ApiResponse.ok(response, "성공적으로 수정되었습니다."));
    }

    // 내부적으로는 soft delete(status 변경)로 처리하지만,
    // 클라이언트 의도는 댓글 리소스 삭제이므로 DELETE 메서드를 사용한다.
    @Operation(summary = "댓글 삭제", description = "댓글 작성자만 댓글을 삭제할 수 있습니다. 내부적으로는 soft delete로 처리합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("postId") Long postId,
            @PathVariable("id") Long id,
            @RequestAttribute("username") String username
    ) {
        commentService.delete(postId, id, username);
        return ResponseEntity.noContent().build();
    }
}
