package com.example.coreboard.domain.common.exception.post;

import com.example.coreboard.domain.common.exception.FieldError;
import org.springframework.http.HttpStatus;

import java.util.List;

public enum PostErrorCode {
    TITLE_AND_CONTENTS_BLANK(
            HttpStatus.BAD_REQUEST,
            400,
            "제목과 내용은 필수입니다.",
            List.of(new FieldError(
                    "title, content",
                    "제목과 본문은 필수입니다."
            ))),
    NOT_TITLE(
            HttpStatus.BAD_REQUEST,
            400,
            "제목은 필수입니다.",
            List.of(new FieldError(
                    "title",
                    "제목은 필수입니다."
            ))),
    NOT_CONTENT(
            HttpStatus.BAD_REQUEST,
            400,
            "본문은 필수입니다.",
            List.of(new FieldError(
                    "content",
                    "본문은 필수입니다."
            ))),
    TITLE_TOO_LONG(
            HttpStatus.BAD_REQUEST,
            400,
            "제목은 255자 이하여야 합니다.",
            List.of(new FieldError(
                    "title",
                    "제목은 255자 이하여야 합니다."
            ))),
    CONTENT_TOO_LONG(
            HttpStatus.BAD_REQUEST,
            400,
            "본문은 10000자 이하여야 합니다.",
            List.of(new FieldError(
                    "content",
                    "본문은 10000자 이하여야 합니다."
            ))),
    POST_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            404,
            "존재하지 않는 게시글입니다.",
            List.of(new FieldError(
                    "postId",
                    "존재하지 않는 게시글입니다."
            ))),
    TITLE_DUPLICATED(
            HttpStatus.CONFLICT,
            409,
            "이미 사용 중인 제목입니다.",
            List.of(new FieldError(
                    "title",
                    "이미 사용 중인 제목입니다."
            ))),
    PAGE_NOT_INTEGER(
            HttpStatus.BAD_REQUEST,
            400,
            "page는 0 이상이어야 합니다.",
            List.of(new FieldError(
                    "page",
                    "page는 0 이상이어야 합니다."
            ))),
    SIZE_TOO_LARGE(
            HttpStatus.BAD_REQUEST,
            400,
            "size는 1 이상 10 이하이어야 합니다.",
            List.of(new FieldError(
                    "size",
                    "size는 1 이상 10 이하이어야 합니다."
            ))),
    SORT_DIRECTION_INVALID(
            HttpStatus.BAD_REQUEST,
            400,
            "정렬 방향은 asc 또는 desc만 허용됩니다.",
            List.of(new FieldError(
                    "sort",
                    "정렬 방향은 asc 또는 desc만 허용됩니다."
            ))),
    POST_ISDELETE(
            HttpStatus.NOT_FOUND,
            404,
            "삭제된 게시글입니다.",
            List.of(new FieldError(
                    "postId",
                    "삭제된 게시글입니다."
            ))),
    ATTACHMENT_COUNT_EXCEEDED(
            HttpStatus.BAD_REQUEST,
            400,
                    "첨부파일 개수가 허용 범위를 초과했습니다.",
            List.of(new FieldError(
                    "attachmentIds",
                    "첨부파일 개수가 허용 범위를 초과했습니다."
    ))),
    ATTACHMENT_REQUIRED(
            HttpStatus.BAD_REQUEST,
            400,
            "첨부파일이 필요한 게시판입니다.",
            List.of(new FieldError(
                    "attachmentIds",
                    "첨부파일이 필요한 게시판입니다."
            ))),
    ATTACHMENT_NOT_ALLOWED(
            HttpStatus.BAD_REQUEST,
            400,
            "이 게시판은 첨부파일을 허용하지 않습니다.",
            List.of(new FieldError(
                    "attachmentIds",
                    "이 게시판은 첨부파일을 허용하지 않습니다."
            ))),
    ;

    private final HttpStatus status;
    private final int code;
    private final String message;
    private final List<FieldError> errors;

    PostErrorCode(
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