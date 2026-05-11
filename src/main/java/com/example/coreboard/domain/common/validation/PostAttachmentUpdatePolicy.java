package com.example.coreboard.domain.common.validation;

import com.example.coreboard.domain.attachment.entity.Attachment;
import com.example.coreboard.domain.board.entity.Board;

import java.util.List;

public class PostAttachmentUpdatePolicy {
    public static void validate(
            Board board,
            List<Attachment> currentAttachments,
            List<Long> keepAttachmentIds,
            List<Long> newAttachmentIds
    ) {
        int keepCount = keepAttachmentIds == null
                ? currentAttachments.size()
                : keepAttachmentIds.size();

        int newCount = newAttachmentIds == null
                ? 0
                : newAttachmentIds.size();

        int finalAttachmentCount = keepCount + newCount;

        PostAttachmentPolicyValidator.validate(board, finalAttachmentCount);
    }
}
