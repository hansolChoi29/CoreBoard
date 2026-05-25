package com.example.coreboard.domain.attachment.exception;

import com.example.coreboard.global.exception.ErrorException;

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
