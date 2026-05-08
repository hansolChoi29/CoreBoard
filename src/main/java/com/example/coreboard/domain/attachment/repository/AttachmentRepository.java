package com.example.coreboard.domain.attachment.repository;

import com.example.coreboard.domain.attachment.entity.Attachment;
import com.example.coreboard.domain.attachment.entity.AttachmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    // 스케줄러용 : TEMP 상태면 24시간 지난 것
    List<Attachment> findByStatusAndCreatedAtBefore(
            AttachmentStatus status,
            LocalDateTime createdAt
    );

    List<Attachment> findByPostIdAndStatus(
            Long postId,
            AttachmentStatus status
    );

    List<Attachment> findByStatusAndDeletedAtBefore(
            AttachmentStatus status,
            LocalDateTime deletedAt
    );
}
