package com.example.coreboard.domain.common.validation;

import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.common.exception.post.PostErrorException;

import static com.example.coreboard.domain.common.exception.post.PostErrorCode.*;

import java.util.List;

public class PostAttachmentPolicyValidator {
       public static void validate(Board board, List<Long> attachmentIds) {
        int count = attachmentIds == null ? 0 : attachmentIds.size();

        if (board.isRequireAttachment() &&
                count == 0) {
            throw new PostErrorException(ATTACHMENT_REQUIRED);
        }

        if (board.getMaxAttachmentCount() == 0 &&
                count > 0) {
            throw new PostErrorException(ATTACHMENT_NOT_ALLOWED);
        }

        if (count > board.getMaxAttachmentCount()) {
            throw new PostErrorException(ATTACHMENT_COUNT_EXCEEDED);
        }
    }
}
