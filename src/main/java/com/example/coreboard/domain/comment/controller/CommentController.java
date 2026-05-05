package com.example.coreboard.domain.comment.controller;

import com.example.coreboard.domain.comment.dto.command.CreateCommentCommand;
import com.example.coreboard.domain.comment.dto.query.GetCommentQuery;
import com.example.coreboard.domain.comment.dto.request.CreateCommentRequest;
import com.example.coreboard.domain.comment.dto.response.CreateCommentResponse;
import com.example.coreboard.domain.comment.dto.response.GetAllCommentResponse;
import com.example.coreboard.domain.common.response.SliceResponse;
import com.example.coreboard.domain.comment.dto.result.CreateCommentResult;
import com.example.coreboard.domain.comment.service.CommentService;
import com.example.coreboard.domain.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/{postId}/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping()
    public ResponseEntity<ApiResponse<CreateCommentResponse>> create(
            @PathVariable Long postId,
            @RequestAttribute("username") String username,
            @RequestBody CreateCommentRequest request
    ) {
        // TODO : validator add (content)
        CreateCommentCommand command = new CreateCommentCommand(request.content());
        CreateCommentResult result = commentService.create(postId, username, command);
        CreateCommentResponse response = new CreateCommentResponse(result.id());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "댓글이 성공적으로 작성되었습니다."));
    }

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
}
