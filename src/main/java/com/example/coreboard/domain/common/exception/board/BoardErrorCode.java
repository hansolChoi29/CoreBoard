package com.example.coreboard.domain.common.exception.board;

public enum BoardErrorCode {

    // 트러블 - 204는 성공으로 떠서 응답바디에 아무것도 안 뜸
    TITLE_AND_CONTENTS_BLANK(400,"제목과 내용은 필수입니다."),
    NOT_TITLE(400,"제목은 필수입니다."),
    NOT_CONTENT(400,"내용은 필수입니다."),
    TITLE_TOO_LONG(400,"제목은 255자 이하여야 합니다"),
    CONTENT_TOO_LONG(400, "본문은 1000자 이하여야 합니다"),
    TITLE_AND_CONTENT_BLANK(400, "제목 또는 본문을 입력해 주세요!"),
    POST_NOT_FOUND(400, "존재하지 않는 게시글입니다."),
    TITLE_DUPLICATED(404,"이미 사용 중인 제목입니다.");

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
