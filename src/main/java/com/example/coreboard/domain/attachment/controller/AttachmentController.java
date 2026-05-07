package com.example.coreboard.domain.attachment.controller;

import com.example.coreboard.domain.attachment.service.AttachmentService;
import com.example.coreboard.domain.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "Attachment", description = "첨부파일 관련 API")
@RestController
@RequestMapping("/attachments")
public class AttachmentController {
    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @Operation(
            summary = "첨부파일 사전 업로드",
            description = "게시글 저장 전에 파일을 먼저 업로드하고, 게시글 생성 시 사용할 첨부파일 ID를 반환"
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
