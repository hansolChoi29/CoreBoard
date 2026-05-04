package com.example.coreboard.domain.attachment.repository;

import com.example.coreboard.domain.attachment.entity.Attachment;
import com.example.coreboard.domain.attachment.entity.AttachmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    // 게시글에 연결된 첨부파일 전체 조회
    List<Attachment> findByPostId(Long postId);

    // 스케줄러용 : TEMP 상태면 24시간 지난 것
    List<Attachment> findByStatusAndCreatedAtBefore(
            AttachmentStatus status,
            LocalDateTime createdAt
    );
}
