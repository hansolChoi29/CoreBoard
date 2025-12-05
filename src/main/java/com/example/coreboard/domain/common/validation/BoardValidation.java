package com.example.coreboard.domain.common.validation;

import ch.qos.logback.core.util.StringUtil;
import com.example.coreboard.domain.board.dto.request.BoardCreateRequest;
import com.example.coreboard.domain.board.dto.request.BoardUpdateRequest;
import com.example.coreboard.domain.common.exception.board.BoardErrorCode;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;

import static com.example.coreboard.domain.common.exception.board.BoardErrorCode.*;
import static com.example.coreboard.domain.common.exception.board.BoardErrorCode.TITLE_AND_CONTENTS_BLANK;

public class BoardValidation {

    public static void createValidation(BoardCreateRequest createRequest) {
        createValidation(createRequest.getTitle(), createRequest.getContent());
    }

    public static void updateValidation(BoardUpdateRequest updateRequest) {
        updateValidation(updateRequest.getTitle(), updateRequest.getContent());
    }

    public static void updateValidation(String title, String content) {
        boolean titleValidation = StringUtil.isNullOrEmpty(title);
        boolean contentValidation = StringUtil.isNullOrEmpty(content);

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
        if (content.length() > 1000) {
            throw new BoardErrorException(CONTENT_TOO_LONG);
        }
    }

    public static void createValidation(String title, String content) {
        boolean titleValidation = StringUtil.isNullOrEmpty(title);
        boolean contentValidation = StringUtil.isNullOrEmpty(content);

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
        if (content.length() > 1000) {
            throw new BoardErrorException(CONTENT_TOO_LONG);
        }
    }

    public static void pageableValication(int page, int size) {
        if (page < 0) {
            throw new BoardErrorException(BoardErrorCode.PAGE_NOT_INTEGER);
        }
        if (size < 1 || size > 10) {
            throw new BoardErrorException(BoardErrorCode.SIZE_TOO_LARGE);
        }
    }

    public static void sortDirection(String sort){
        if (!sort.equalsIgnoreCase("asc") &&
                !sort.equalsIgnoreCase("desc")) {
            throw new BoardErrorException(BoardErrorCode.SORT_DIRECTION_INVALID);
        }
    }
}