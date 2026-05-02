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
                    "NOT FOUND BOARD",
                    "존재하지 않는 게시판입니다."
            ))),
    BOARD_SLUG_DUPLICATE(
            HttpStatus.CONFLICT,
            409,
            "이미 사용 중인 게시판 주소입니다. 다른 주소를 입력해 주세요.",
            List.of(new FieldError(
                    "SLUG DUPLICATE",
                    "이미 사용 중인 게시판 주소입니다. 다른 주소를 입력해 주세요."
            ))),
    BOARD_NAME_DUPLICATE(
            HttpStatus.CONFLICT,
            409,
            "이미 사용 중인 게시판 이름입니다. 다른 이름을 입력해 주세요.",
            List.of(new FieldError(
                    "SLUG DUPLICATE",
                    "이미 사용 중인 게시판 이름입니다. 다른 이름을 입력해 주세요."
            ))),
    BOARD_HAS_POSTS(
            HttpStatus.CONFLICT,
            409,
            "게시판에 게시글이 남아 있어 삭제할 수 없습니다. 게시글을 먼저 삭제해 주세요.",
            List.of(new FieldError(
                    "SLUG DUPLICATE",
                    "게시판에 게시글이 남아 있어 삭제할 수 없습니다. 게시글을 먼저 삭제해 주세요."
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
