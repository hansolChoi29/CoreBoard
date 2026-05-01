package com.example.coreboard.domain.board.controller;


import com.example.coreboard.domain.board.dto.command.UpdateBoardCommand;
import com.example.coreboard.domain.board.dto.request.UpdateBoardRequest;
import com.example.coreboard.domain.board.dto.response.UpdateBoardResponse;
import com.example.coreboard.domain.board.dto.result.CreateBoardResult;
import com.example.coreboard.domain.board.dto.command.CreateBoardCommand;
import com.example.coreboard.domain.board.dto.request.CreateBoardRequest;
import com.example.coreboard.domain.board.dto.response.CreateBoardResponse;
import com.example.coreboard.domain.board.dto.result.UpdateBoardResult;
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
    // TODO : 입력값 검증 null, isBlank - create  & update
    // TODO : 삭제 시 본인 아님 - delete
    // TODO : slug 중복 - - create  & update
    // TODO : name 길이고민 좀 - create  & update
    // TODO : 첨부파일 최대개수 초과 - create  & update
    // TODO : name 중복- create  & update
    // TODO : 단건조회 시 id 유효하지 않음 (음수, 문자)
    // TODO : 존재하지 않은 게시판
    // TODO : 전체조회 제외하고 비로그인(!ADMIN)접근 불가
    // 삭제 - 비활성화  TODO : id 잘못됨, 본인아님(!ADMIN), 게시글 존재하면 삭제 불가
    //
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

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UpdateBoardResponse>> update(
            @PathVariable("id") Long id,
            @RequestAttribute("username") String username,
            @RequestBody UpdateBoardRequest request
    ) {
        UpdateBoardCommand command = new UpdateBoardCommand(
                request.id(),
                request.name(),
                request.slug(),
                request.answerAcceptedEnabled(),
                request.commentEnabled(),
                request.requireAttachment(),
                request.maxAttachmentCount(),
                request.maxContentLength()
        );
        UpdateBoardResult result = boardService.update(command, username, id);
        UpdateBoardResponse response = new UpdateBoardResponse(result.id());
        return ResponseEntity.ok(ApiResponse.ok(response, "성공적으로 수정되었습니다."));
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
