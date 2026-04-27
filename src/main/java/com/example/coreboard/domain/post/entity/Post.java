package com.example.coreboard.domain.post.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotBlank(message = "제목은 필수입니다")
    @Column(name = "title", nullable = false)
    private String title;

    @Lob
    @NotBlank(message = "내용은 필수입니다")
    @Column(name = "content", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentFormat contentFormat;
    /*
          게시글 삭제 시 DB에서 바로 삭제할 수도 있지만,
          운영 서비스에서는 흔적을 남겨서 복구/분쟁/대응/장애 분석 등에 쓰일 수도 있다.
        */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status = PostStatus.PUBLISHED;

    // TODO adr-0003
    @Column(nullable = false)
    private Long viewCount = 0L; // 조회

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column
    private LocalDateTime lastModifiedDate;

    protected Post() {
    }

    public Post(
            Long boardId,
            Long userId,
            String title,
            String content,
            ContentFormat contentFormat
    ) {
        this.boardId = boardId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.contentFormat = contentFormat;
        this.status = PostStatus.PUBLISHED;
        this.viewCount = 0L;
    }

    public static Post create(
            Long boardId,
            Long userId,
            String title,
            String content,
            ContentFormat contentFormat
    ) {
        return new Post(
                boardId,
                userId,
                title,
                content,
                contentFormat
        );
    }

    public void delete() {
        this.status = PostStatus.DELETED;
    }

    public boolean isWrittenBy(Long userId) {
        return this.userId.equals(userId);
    }

    public Long getBoardId() {
        return boardId;
    }

    public ContentFormat getContentFormat() {
        return contentFormat;
    }

    public PostStatus getStatus() {
        return status;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void update(
            String newTitle,
            String newContent,
            ContentFormat newContentFormat
    ) {
        if (newTitle != null && !newTitle.isBlank()) {
            this.title = newTitle;
        }

        if (newContent != null && !newContent.isBlank()) {
            this.content = newContent;
        }

        if (newContentFormat != null) {
            this.contentFormat = newContentFormat;
        }
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }
}


