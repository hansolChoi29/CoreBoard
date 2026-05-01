package com.example.coreboard.domain.board.controller;


import com.example.coreboard.domain.board.dto.command.GetOneBoardCommand;
import com.example.coreboard.domain.board.dto.query.GetBoardListQuery;
import com.example.coreboard.domain.board.dto.response.GetBoardListResponse;
import com.example.coreboard.domain.board.dto.response.GetOneBoardResponse;
import com.example.coreboard.domain.board.dto.result.GetOneBoardResult;
import com.example.coreboard.domain.board.service.BoardService;
import com.example.coreboard.domain.common.response.ApiResponse;
import com.example.coreboard.domain.common.response.OffsetPageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Board", description = "게시판 관련 API")
@RestController
@RequestMapping("/boards")
public class BoardController {
    private final BoardService boardService;
    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GetOneBoardResponse>> getOne(
            @PathVariable("id") Long id
    ) {
        GetOneBoardCommand command = new GetOneBoardCommand(id);
        GetOneBoardResult out = boardService.getOne(command);
        GetOneBoardResponse response = new GetOneBoardResponse(
                out.id(),
                out.name(),
                out.slug(),
                out.answerAcceptedEnabled(),
                out.commentEnabled(),
                out.requireAttachment(),
                out.maxAttachmentCount(),
                out.maxContentLength(),
                out.requiredWriteRole(),
                out.posts()
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "성공적으로 불러왔습니다."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<OffsetPageResponse<GetBoardListResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        GetBoardListQuery query = new GetBoardListQuery(page, size, direction);
        OffsetPageResponse<GetBoardListResponse> response = boardService.getAll(query);
        return ResponseEntity.ok(ApiResponse.ok(response, "성공적으로 불러왔습니다."));
    }
}
