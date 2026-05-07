package com.example.coreboard.domain.common.validation;

import com.example.coreboard.domain.board.dto.request.CreateBoardRequest;
import com.example.coreboard.domain.board.dto.request.UpdateBoardRequest;
import com.example.coreboard.domain.common.exception.board.BoardErrorException;
import com.example.coreboard.domain.users.entity.UserRole;

import static com.example.coreboard.domain.common.exception.board.BoardErrorCode.*;
import static io.micrometer.common.util.StringUtils.isBlank;

public class BoardValidation {
    private static final int BOARD_NAME_MIN_LENGTH = 2;
    private static final int BOARD_NAME_MAX_LENGTH = 20;

    private static final int BOARD_SLUG_MIN_LENGTH = 2;
    private static final int BOARD_SLUG_MAX_LENGTH = 50;

    private static final int BOARD_ATTACHMENT_COUNT_MIN = 0;
    private static final int BOARD_ATTACHMENT_COUNT_MAX = 2;

    private static final String BOARD_SLUG_PATTERN = "^[a-z0-9]+(-[a-z0-9]+)*$";

    public static void validateForCreate(CreateBoardRequest request) {
        validateBoardSettings(
                request.name(),
                request.slug(),
                request.requireAttachment(),
                request.maxAttachmentCount()
        );

        validateRequiredWriteRole(request.allowedWriteRoles());
    }

    public static void validateForUpdate(UpdateBoardRequest request) {
        validateBoardSettings(
                request.name(),
                request.slug(),
                request.requireAttachment(),
                request.maxAttachmentCount()
        );
    }

    public static void validateBoardSettings(
            String name,
            String slug,
            boolean requireAttachment,
            int maxAttachmentCount
    ) {
        validateName(name);
        validateSlug(slug);
        validateMaxAttachmentCount(maxAttachmentCount);
        validateAttachmentPolicy(requireAttachment, maxAttachmentCount);
    }

    public static void validateName(String name) {
        if (isBlank(name)) {
            throw new BoardErrorException(BOARD_NAME_REQUIRED);
        }

        if (name.length() < BOARD_NAME_MIN_LENGTH || name.length() > BOARD_NAME_MAX_LENGTH) {
            throw new BoardErrorException(BOARD_NAME_LENGTH_INVALID);
        }
    }

    public static void validateSlug(String slug) {
        if (isBlank(slug)) {
            throw new BoardErrorException(BOARD_SLUG_REQUIRED);
        }

        if (slug.length() < BOARD_SLUG_MIN_LENGTH || slug.length() > BOARD_SLUG_MAX_LENGTH) {
            throw new BoardErrorException(BOARD_SLUG_LENGTH_INVALID);
        }

        if (!slug.matches(BOARD_SLUG_PATTERN)) {
            throw new BoardErrorException(BOARD_SLUG_FORMAT_INVALID);
        }
    }

    public static void validateMaxAttachmentCount(int maxAttachmentCount) {
        if (maxAttachmentCount < BOARD_ATTACHMENT_COUNT_MIN || maxAttachmentCount > BOARD_ATTACHMENT_COUNT_MAX) {
            throw new BoardErrorException(MAX_ATTACHMENT_COUNT_INVALID);
        }
    }

    public static void validateRequiredWriteRole(UserRole requiredWriteRole) {
        if (requiredWriteRole == null) {
            throw new BoardErrorException(REQUIRED_WRITE_ROLE_REQUIRED);
        }
    }

    public static void validateAttachmentPolicy(
            boolean attachmentEnabled,
            int maxAttachmentCount
    ) {
        if (!attachmentEnabled && maxAttachmentCount != 0) {
            throw new BoardErrorException(ATTACHMENT_POLICY_INVALID);
        }

        if (attachmentEnabled && maxAttachmentCount == 0) {
            throw new BoardErrorException(ATTACHMENT_POLICY_INVALID);
        }
    }
}