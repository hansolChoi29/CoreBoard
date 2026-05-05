package com.example.coreboard.domain.common.exception.board;

import com.example.coreboard.domain.common.exception.FieldError;
import org.springframework.http.HttpStatus;

import java.util.List;

public enum BoardErrorCode {
    BOARD_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            404,
            "존재하지 않는 게시판입니다.",
            List.of(new FieldError(
                    "boardId",
                    "존재하지 않는 게시판입니다."
            ))),
    BOARD_SLUG_DUPLICATE(
            HttpStatus.CONFLICT,
            409,
            "이미 사용 중인 게시판 주소입니다. 다른 주소를 입력해 주세요.",
            List.of(new FieldError(
                    "slug",
                    "이미 사용 중인 게시판 주소입니다. 다른 주소를 입력해 주세요."
            ))),
    BOARD_NAME_DUPLICATE(
            HttpStatus.CONFLICT,
            409,
            "이미 사용 중인 게시판 이름입니다. 다른 이름을 입력해 주세요.",
            List.of(new FieldError(
                    "name",
                    "이미 사용 중인 게시판 이름입니다. 다른 이름을 입력해 주세요."
            ))),
    BOARD_HAS_POSTS(
            HttpStatus.CONFLICT,
            409,
            "게시판에 게시글이 남아 있어 삭제할 수 없습니다. 게시글을 먼저 삭제해 주세요.",
            List.of(new FieldError(
                    "boardId",
                    "게시판에 게시글이 남아 있어 삭제할 수 없습니다. 게시글을 먼저 삭제해 주세요."
            ))),
    // 입력값 예외
    BOARD_NAME_REQUIRED(
            HttpStatus.BAD_REQUEST,
            400,
            "게시판 이름은 필수입니다.",
            List.of(new FieldError(
                    "name",
                    "게시판 이름은 필수입니다."
            ))),
    BOARD_NAME_LENGTH_INVALID(
            HttpStatus.BAD_REQUEST,
            400,
            "게시판 이름은 2자 이상 20자 이하로 입력해 주세요.",
            List.of(new FieldError(
                    "name",
                    "게시판 이름은 2자 이상 20자 이하로 입력해 주세요."
            ))),
    BOARD_SLUG_REQUIRED(
            HttpStatus.BAD_REQUEST,
            400,
            "게시판 주소는 필수입니다.",
            List.of(new FieldError(
                    "slug",
                    "게시판 주소는 필수입니다."
            ))),
    BOARD_SLUG_LENGTH_INVALID(
            HttpStatus.BAD_REQUEST,
            400,
            "게시판 주소는 2자 이상 50자 이하로 입력해 주세요.",
            List.of(new FieldError(
                    "slug",
                    "게시판 주소는 2자 이상 50자 이하로 입력해 주세요."
            ))),
    BOARD_SLUG_FORMAT_INVALID(
            HttpStatus.BAD_REQUEST,
            400,
            "게시판 주소는 영문 소문자, 숫자, 하이픈만 사용할 수 있습니다.",
            List.of(new FieldError(
                    "slug",
                    "게시판 주소는 영문 소문자, 숫자, 하이픈만 사용할 수 있습니다."
            ))),
    MAX_ATTACHMENT_COUNT_INVALID(
            HttpStatus.BAD_REQUEST,
            400,
            "최대 첨부파일 개수는 0개 이상 2개 이하로 입력해 주세요.",
            List.of(new FieldError(
                    "maxAttachmentCount",
                    "최대 첨부파일 개수는 0개 이상 2개 이하로 입력해 주세요."
            ))),
    REQUIRED_WRITE_ROLE_REQUIRED(
            HttpStatus.BAD_REQUEST,
            400,
            "게시글 작성 권한은 필수입니다.",
            List.of(new FieldError(
                    "requiredWriteRole",
                    "게시글 작성 권한은 필수입니다."
            ))),
    REQUIRED_WRITE_ROLE_INVALID(
            HttpStatus.BAD_REQUEST,
            400,
            "게시글 작성 권한은 USER 또는 ADMIN만 사용할 수 있습니다.",
            List.of(new FieldError(
                    "requiredWriteRole",
                    "게시글 작성 권한은 USER 또는 ADMIN만 사용할 수 있습니다."
            ))),
    ATTACHMENT_POLICY_INVALID(
            HttpStatus.BAD_REQUEST,
            400,
            "첨부파일을 필수로 설정하려면 최대 첨부파일 개수는 1개 이상이어야 합니다.",
            List.of(new FieldError(
                    "maxAttachmentCount",
                    "첨부파일을 필수로 설정하려면 최대 첨부파일 개수는 1개 이상이어야 합니다."
            )));

    private final HttpStatus status;
    private final int code;
    private final String message;
    private final List<FieldError> errors;

    BoardErrorCode(
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
