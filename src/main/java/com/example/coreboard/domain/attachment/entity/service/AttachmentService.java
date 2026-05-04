package com.example.coreboard.domain.attachment.entity.service;

import com.example.coreboard.domain.attachment.entity.repository.AttachmentRepository;
import org.springframework.stereotype.Service;

@Service
public class AttachmentService {
    private final AttachmentRepository attachmentRepository;

    public AttachmentService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }


}
