package com.example.coreboard.domain.board.controller;


import com.example.coreboard.domain.board.dto.result.CreateBoardResult;
import com.example.coreboard.domain.board.dto.command.CreateBoardCommand;
import com.example.coreboard.domain.board.dto.request.CreateBoardRequest;
import com.example.coreboard.domain.board.dto.response.CreateBoardResponse;
import com.example.coreboard.domain.board.service.BoardService;
import com.example.coreboard.domain.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Board", description = "관리자권한 게시판 관련 API")
@RestController
@RequestMapping("/admin/boards")
public class AdminBoardController {
    private final BoardService boardService;
    public AdminBoardController(BoardService boardService) {
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
        CreateBoardResult out = boardService.create(command, username);
        CreateBoardResponse response = new CreateBoardResponse(out.id());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "성공적으로 게시판이 생성되었습니다."));
    }

}/* {
  "content": [
    {
      "userId": 1,
      "username": "admin01",
      "role": "ADMIN"
    }
  ],
  "pageInfo": {
    "page": 0,
    "size": 20,
    "totalElements": 53,
    "totalPages": 3
  }
}
*/
