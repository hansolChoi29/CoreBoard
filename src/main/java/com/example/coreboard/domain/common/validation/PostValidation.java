package com.example.coreboard.domain.common.validation;

import ch.qos.logback.core.util.StringUtil;
import com.example.coreboard.domain.post.dto.request.CreatePostRequest;
import com.example.coreboard.domain.post.dto.request.UpdatePostRequest;
import com.example.coreboard.domain.common.exception.post.PostErrorCode;
import com.example.coreboard.domain.common.exception.post.PostErrorException;

import static com.example.coreboard.domain.common.exception.post.PostErrorCode.*;
import static com.example.coreboard.domain.common.exception.post.PostErrorCode.TITLE_AND_CONTENTS_BLANK;

public class PostValidation {
    public static void createValidation(CreatePostRequest createRequest) {
        createValidation(createRequest.title(), createRequest.content());
    }

    public static void updateValidation(UpdatePostRequest updateRequest) {
        updateValidation(updateRequest.title(), updateRequest.content());
    }

    public static void updateValidation(String title, String content) {
        boolean titleValidation = StringUtil.isNullOrEmpty(title);
        boolean contentValidation = StringUtil.isNullOrEmpty(content);

        if (titleValidation && contentValidation) {
            throw new PostErrorException(TITLE_AND_CONTENTS_BLANK);
        }
        if (titleValidation) {
            throw new PostErrorException(NOT_TITLE);
        }
        if (contentValidation) {
            throw new PostErrorException(NOT_CONTENT);
        }
        if (title.length() > 255) {
            throw new PostErrorException(TITLE_TOO_LONG);
        }
        if (content.length() > 1000) {
            throw new PostErrorException(CONTENT_TOO_LONG);
        }
    }

    public static void createValidation(String title, String content) {
        boolean titleValidation = StringUtil.isNullOrEmpty(title);
        boolean contentValidation = StringUtil.isNullOrEmpty(content);

        if (titleValidation && contentValidation) {
            throw new PostErrorException(TITLE_AND_CONTENTS_BLANK);
        }
        if (titleValidation) {
            throw new PostErrorException(NOT_TITLE);
        }
        if (contentValidation) {
            throw new PostErrorException(NOT_CONTENT);
        }
        if (title.length() > 255) {
            throw new PostErrorException(TITLE_TOO_LONG);
        }
        if (content.length() > 1000) {
            throw new PostErrorException(CONTENT_TOO_LONG);
        }
    }

    public static void pageableValication(int size) {
        if (size < 1 || size > 10) {
            throw new PostErrorException(PostErrorCode.SIZE_TOO_LARGE);
        }
    }

    public static void sortDirection(String sort) {
        if (!sort.equalsIgnoreCase("asc") &&
                !sort.equalsIgnoreCase("desc")) {
            throw new PostErrorException(PostErrorCode.SORT_DIRECTION_INVALID);
        }
    }
}