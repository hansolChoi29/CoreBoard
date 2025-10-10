package com.example.coreboard.domain.common.exception.board;

public enum BoardErrorCode {

    // C , U
    // 제목은 255자 이하여야 합니다
    // 본문은 1000자 이하여야 합니다
    // 제목과 본문이 null
    TITLE_TOO_LONG(400,"제목은 255자 이하여야 합니다"),
    CONTENT_TOO_LONG(400, "본문은 1000자 이하여야 합니다"),
    TITLE_AND_CONTENT_BLANK(400, "제목 또는 본문을 입력해 주세요!"),
    // R
    // 존재하지 않는 게시글입니다.
    POST_NOT_FOUND(400, "존재하지 않는 게시글입니다."),

    // U
    // 제목은 255자 이하여야 합니다
    // 본문은 1000자 이하여야 합니다
    // 제목과 본문이 null

    // D
    // 존재하지 않는 게시글입니다.
    // (본인 게시글이 아님) 삭제 권한이 없습니다.
    DELETE_UNAUTHORIZED(400, "존재하지 않는 게시글입니다.");

    private final int status;
    private final String message;

    BoardErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
