package com.example.coreboard.domain.board.controller;


import com.example.coreboard.domain.board.dto.CreateBoardDto;
import com.example.coreboard.domain.board.dto.GetOneBoardDto;
import com.example.coreboard.domain.board.dto.command.CreateBoardCommand;
import com.example.coreboard.domain.board.dto.command.GetOneBoardCommand;
import com.example.coreboard.domain.board.dto.request.CreateBoardRequest;
import com.example.coreboard.domain.board.dto.response.CreateBoardResponse;
import com.example.coreboard.domain.board.dto.response.GetOneBoardResponse;
import com.example.coreboard.domain.board.service.BoardService;
import com.example.coreboard.domain.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Board", description = "게시판 관련 API")
@RestController
@RequestMapping("/admin/boards")
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateBoardResponse>> create(
            @RequestBody CreateBoardRequest request,
            @RequestAttribute("username") String username
    ) {
        // TODO : slug 정책 추가 및, 입력검증 추가
        CreateBoardCommand command = new CreateBoardCommand(
                request.name(),
                request.slug(),
                request.answerAcceptedEnabled(),
                request.commentEnabled(),
                request.requireAttachment(),
                request.maxAttachmentCount(),
                request.maxContentLength(),
                request.requiredWriteRole()
        );
        CreateBoardDto out = boardService.create(command, username);
        CreateBoardResponse response = new CreateBoardResponse(out.id());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "성공적으로 게시판이 생성되었습니다."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GetOneBoardResponse>> getOne(
            @PathVariable("id") Long id
    ) {
        GetOneBoardCommand command = new GetOneBoardCommand(id);
        GetOneBoardDto out = boardService.getOne(command);
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
}
