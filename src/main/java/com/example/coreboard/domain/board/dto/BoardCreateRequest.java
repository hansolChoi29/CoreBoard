package com.example.coreboard.domain.board.dto;

import static com.example.coreboard.domain.common.exception.board.BoardErrorCode.*;

import ch.qos.logback.core.util.StringUtil;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;

public class BoardCreateRequest {
    // 생성요청 DTO는 Board의 Id가 필요없어서 수정요청 DTO와 별도로 사용되어야 함

    // 요청 넣어야 하는 것
    private String title;
    private String contents;

    public BoardCreateRequest(
            String title,
            String contents
    ) {
        this.title = title;
        this.contents = contents;
    }

    // 글자 수 및 빈값 예외처리
    public void validation() {
        boolean titleValidation = StringUtil.isNullOrEmpty(title);
        boolean contentValidation = StringUtil.isNullOrEmpty(contents);

        if (titleValidation && contentValidation) {
            throw new BoardErrorException(TITLE_AND_CONTENTS_BLANK);
        }
        if (titleValidation) {
            throw new BoardErrorException(NOT_TITLE);
        }
        if (contentValidation) {
            throw new BoardErrorException(NOT_CONTENT);
        }
        if (title.length() > 255) {
            throw new BoardErrorException(TITLE_TOO_LONG);
        }
        if (contents.length() > 1000) {
            throw new BoardErrorException(CONTENT_TOO_LONG);
        }

        this.title = title;
        this.contents = contents;
    }

    public String getBoardTitle() {
        return title;
    }

    public String getBoardContents() {
        return contents;
    }
}
