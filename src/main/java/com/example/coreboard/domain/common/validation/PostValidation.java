package com.example.coreboard.domain.common.validation;

import com.example.coreboard.domain.post.dto.request.CreatePostRequest;
import com.example.coreboard.domain.post.dto.request.UpdatePostRequest;
import com.example.coreboard.domain.common.exception.post.PostErrorException;

import static com.example.coreboard.domain.common.exception.post.PostErrorCode.*;
import static com.example.coreboard.domain.common.exception.post.PostErrorCode.TITLE_AND_CONTENTS_BLANK;
import static io.micrometer.common.util.StringUtils.isBlank;

public class PostValidation {
    private static final int MAX_TITLE_LENGTH = 255;
    private static final int MAX_CONTENT_LENGTH = 10000;
    private static final int MIN_PAGE_SIZE = 1;
    private static final int MAX_PAGE_SIZE = 10;

    public static void validateForCreate(CreatePostRequest request) {
        validateTitleAndContent(request.title(), request.content());
    }

    public static void validateForUpdate(UpdatePostRequest request) {
        validateTitleAndContent(request.title(), request.content());
    }

    public static void validateTitleAndContent(String title, String content) {
        validateNotBothBlank(title, content);
        validateTitle(title);
        validateContent(content);
    }

    public static void validateNotBothBlank(String title, String content) {
        if (isBlank(title) && isBlank(content)) {
            throw new PostErrorException(TITLE_AND_CONTENTS_BLANK);
        }
    }

    public static void validateTitle(String title) {
        if (isBlank(title)) {
            throw new PostErrorException(NOT_TITLE);
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new PostErrorException(TITLE_TOO_LONG);
        }
    }

    public static void validateContent(String content) {
        if (isBlank(content)) {
            throw new PostErrorException(NOT_CONTENT);
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new PostErrorException(CONTENT_TOO_LONG);
        }
    }

    public static void validatePageSize(int size) {
        if (size < MIN_PAGE_SIZE || size > MAX_PAGE_SIZE) {
            throw new PostErrorException(SIZE_TOO_LARGE);
        }
    }

    public static void validateSortDirection(String sort) {
        if (isBlank(sort)) {
            throw new PostErrorException(SORT_DIRECTION_INVALID);
        }
        if (!isAsc(sort) && !isDesc(sort)) {
            throw new PostErrorException(SORT_DIRECTION_INVALID);
        }
    }

    private static boolean isAsc(String sort) {
        return sort.equalsIgnoreCase("asc");
    }

    private static boolean isDesc(String sort) {
        return sort.equalsIgnoreCase("desc");
    }
}