package com.example.coreboard.domain.common.exception.board;

public enum BoardErrorCode {

    // 트러블 - 204는 성공으로 떠서 응답바디에 아무것도 안 뜸
    TITLE_AND_CONTENTS_BLANK(400,"제목과 내용은 필수입니다."),
    NOT_TITLE(400,"제목은 필수입니다."),
    NOT_CONTENT(400,"내용은 필수입니다."),
    TITLE_TOO_LONG(400,"제목은 255자 미만이어야 합니다"),
    CONTENT_TOO_LONG(400, "본문은 1000자 미만이어야 합니다"),
    POST_NOT_FOUND(404, "존재하지 않는 게시글입니다."),
    TITLE_DUPLICATED(409,"이미 사용 중인 제목입니다."),
    PAGE_NEGATICE(400,"page는 0이상이어야 합니다."),
    PAGE_NOT_INTERGER(400,"잘못된 요청 형식입니다."),
    SIZE_TOO_LARGE(400,"zise는 최대 10이하이어야 합니다"),
    SORT_DIRECTION_INVALID(400, "정렬 방향은 asc 또는 desc만 허용됩니다.");

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
