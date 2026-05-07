package com.example.coreboard.domain.common.exception.Attachment;

import com.example.coreboard.domain.common.exception.ErrorException;

public class AttachmentErrorException extends ErrorException {
    public AttachmentErrorException(AttachmentErrorCode attachmentErrorCode) {
        super(
                attachmentErrorCode.getStatus(),
                attachmentErrorCode.getCode(),
                attachmentErrorCode.getMessage(),
                attachmentErrorCode.getErrors()
        );
    }
}
