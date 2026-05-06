package com.example.coreboard.domain.common.exception.Attachment;

import com.example.coreboard.domain.common.exception.FieldError;
import org.springframework.http.HttpStatus;

import java.util.List;

public enum AttachmentErrorCode {
    ATTACHMENT_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            403,
            "해당 첨부파일에 접근할 권한이 없습니다.",
            List.of(new FieldError(
                    "attachmentIds",
                    "현재 사용자가 업로드한 첨부파일만 사용할 수 있습니다."
            ))),
    ATTACHMENT_ALREADY_CONFIRMED(
            HttpStatus.CONFLICT,
            409,
            "이미 게시글에 연결된 첨부파일입니다.",
            List.of(new FieldError(
                    "attachmentIds",
                    "TEMP 상태의 첨부파일만 게시글에 연결할 수 있습니다."
            ))),
    ATTACHMENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            404,
            "존재하지 않는 첨부파일입니다.",
            List.of(new FieldError(
                    "attachmentIds",
                    "요청한 첨부파일 ID 중 존재하지 않는 값이 있습니다."
            ))),
    FILE_SIZE_EXCEEDED(
            HttpStatus.PAYLOAD_TOO_LARGE,
            413,
            "첨부파일 크기가 허용된 최대 용량을 초과했습니다.",
            List.of(new FieldError(
                    "file",
                    "첨부파일은 10MB 이하만 업로드할 수 있습니다."
            ))),
    ;

    private final HttpStatus status;
    private final int code;
    private final String message;
    private final List<FieldError> errors;

    AttachmentErrorCode(
            HttpStatus status,
            int code,
            String message,
            List<FieldError> errors
    ) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.errors = errors;
    }

    public int getCode() {
        return code;
    }

    public List<FieldError> getErrors() {
        return errors;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
