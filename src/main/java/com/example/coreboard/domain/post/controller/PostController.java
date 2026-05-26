package com.example.coreboard.domain.post.controller;

import com.example.coreboard.domain.post.service.PopularPostService;
import com.example.coreboard.global.response.OffsetPageResponse;
import com.example.coreboard.global.response.SliceResponse;
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
import com.example.coreboard.domain.post.validation.PostValidation;
import com.example.coreboard.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.coreboard.domain.post.support.ViewerKeyGenerator;

import java.util.List;

@Tag(name = "Post", description = "게시판에 종속되지 않은 게시글 관련 API")
@RestController
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;
    private final ViewerKeyGenerator viewerKeyGenerator;
    private final PopularPostService popularPostService;

    public PostController(
            PostService postService,
            ViewerKeyGenerator viewerKeyGenerator,
            PopularPostService popularPostService
    ) {
        this.postService = postService;
        this.viewerKeyGenerator = viewerKeyGenerator;
        this.popularPostService = popularPostService;
    }

    @Operation(
            summary = "전체 게시글 목록 조회",
            description = "게시판에 종속되지 않은 전체 게시글 목록을 페이지 단위로 조회합니다. 정렬 방향은 sort 파라미터로 지정합니다."
    )
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

    @Operation(
            summary = "게시글 단건 조회",
            description = "게시글 id로 게시글 상세 정보를 조회합니다. 로그인 없이 조회할 수 있으며, 조회 시 Redis를 통해 중복 조회를 방지하고 조회수 증가 이벤트를 기록합니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GetOnePostResponse>> getOne(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {
        String viewerKey = viewerKeyGenerator.generate(request);
        GetOnePostCommand command = new GetOnePostCommand(id, viewerKey);

        GetOnePostResult out = postService.getOne(command);

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


    @Operation(
            summary = "게시글 검색 조회",
            description = "키워드를 기준으로 전체 게시글을 검색합니다. 검색 결과는 Slice 방식으로 반환하며, 다음 페이지 존재 여부를 함께 제공합니다."
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<SliceResponse<PostSummaryResponse>>> search(
            @RequestParam(name = "keyword") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        PostValidation.validatePageSize(size);

        SliceResponse<PostSummaryResponse> response = postService.searchPosts(
                page,
                size,
                keyword
        );

        return ResponseEntity.ok(ApiResponse.ok(response, "게시글 검색 조회!"));
    }

    @Operation(
            summary = "게시글 수정",
            description = "게시글 작성자 또는 ADMIN 권한을 가진 사용자만 게시글 제목, 내용, 본문 형식, 첨부파일 구성을 수정할 수 있습니다."
    )
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

    @Operation(
            summary = "게시글 삭제",
            description = "게시글 작성자 또는 ADMIN 권한을 가진 사용자만 게시글을 삭제할 수 있습니다. 삭제 시 게시글은 삭제 상태로 변경됩니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestAttribute("username") String username,
            @PathVariable("id") Long id
    ) {
        DeletePostCommand command = new DeletePostCommand(id, username);
        postService.delete(command);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "인기글 목록 조회",
            description = "Redis ZSET에 저장된 전체 게시글 조회수 점수를 기준으로 인기글 목록을 조회합니다."
    )
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<PostSummaryResponse>>> popular(
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        PostValidation.validatePageSize(size);

        List<PostSummaryResponse> response = popularPostService.getPopularPosts(size);

        return ResponseEntity.ok(ApiResponse.ok(response, "인기목록 조회 성공!"));
    }
}