package com.example.coreboard.domain.attachment.controller;

import com.example.coreboard.domain.attachment.service.AttachmentService;
import com.example.coreboard.domain.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "Attachment", description = "첨부파일 업로드 API")
@RestController
@RequestMapping("/attachments")
public class AttachmentController {
    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @Operation(
            summary = "첨부파일 사전 업로드",
            description = """
                    게시글 저장 전에 첨부파일을 먼저 임시 업로드합니다.
                    
                    업로드 성공 시 게시글 생성/수정 요청에서 사용할 attachmentId를 반환합니다.
                    이후 게시글 저장 시 해당 attachmentId를 전달하면 TEMP 상태의 첨부파일이 CONFIRMED 상태로 확정됩니다.
                    """
    )
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Long>> upload(
            @RequestAttribute("username") String username,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        Long attachmentId = attachmentService.upload(username, file);

        return ResponseEntity.ok(ApiResponse.ok(attachmentId, "파일 업로드 성공"));
    }
}
