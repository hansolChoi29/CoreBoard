package com.example.coreboard.domain.attachment.entity;


import com.example.coreboard.domain.attachment.exception.AttachmentErrorCode;
import com.example.coreboard.domain.attachment.exception.AttachmentErrorException;
import com.example.coreboard.domain.post.entity.Post;
import com.example.coreboard.domain.users.entity.Users;
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

    @Column(nullable = false)
    private String storeUrl;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String contentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttachmentStatus status;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String objectKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column
    private LocalDateTime deletedAt;

    protected Attachment() {
    }

    public static Attachment createTemp(
            Users user,
            String originalFileName,
            String objectKey,
            String storedUrl,
            String contentType,
            Long fileSize
    ) {
        Attachment attachment = new Attachment();
        attachment.user = user;
        attachment.originalFileName = originalFileName;
        attachment.objectKey = objectKey;
        attachment.storeUrl = storedUrl;
        attachment.contentType = contentType;
        attachment.fileSize = fileSize;
        attachment.status = AttachmentStatus.TEMP;
        return attachment;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void validateOwner(Users user) {
        if (!this.user.getUserId().equals(user.getUserId())) {
            throw new AttachmentErrorException(AttachmentErrorCode.ATTACHMENT_FORBIDDEN);
        }
    }

    public void validateTemp() {
        if (this.status != AttachmentStatus.TEMP) {
            throw new AttachmentErrorException(AttachmentErrorCode.ATTACHMENT_ALREADY_CONFIRMED);
        }
    }

    public void markDeleted() {
        this.status = AttachmentStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public String getObjectKey() {
        return objectKey;
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
