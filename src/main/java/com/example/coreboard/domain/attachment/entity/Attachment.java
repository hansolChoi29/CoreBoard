package com.example.coreboard.domain.attachment.entity;


import com.example.coreboard.domain.post.entity.Post;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "attachment")
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // url 저장
    @Column(nullable = false)
    private String storeUrl;
    // 원본 파일명
    @Column(nullable = false)
    private String originalFileName;
    // 이미지냐 파일이냐
    @Column(nullable = false)
    private String contentType;
    // 어떤 게시글의 첨부파일이냐
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;
    // 고아파일 정리 스케줄링
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttachmentStatus status;
    // 생성시간 (24시간 이상 된 TEMP 파일 삭제할 때 기준)
    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;
    // 파일 크기 (10MB 초과면 업로드 막을 용도)
    @Column(nullable = false)
    private Long fileSize;

    protected Attachment() {
    }

    public static Attachment createTemp(
            String originalFileName,
            String storedUrl,
            String contentType,
            Long fileSize
    ) {
        Attachment attachment = new Attachment();
        attachment.originalFileName = originalFileName;
        attachment.storeUrl = storedUrl;
        attachment.contentType = contentType;
        attachment.fileSize = fileSize;
        attachment.status = AttachmentStatus.TEMP;
        return attachment;
    }

    public void confirm(Post post) {
        this.post = post;
        this.status = AttachmentStatus.CONFIRMED;
    }

    public Long getId() {
        return id;
    }

    public String getStoreUrl() {
        return storeUrl;
    }

    public AttachmentStatus getStatus() {
        return status;
    }
}
