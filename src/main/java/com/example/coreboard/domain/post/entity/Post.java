package com.example.coreboard.domain.post.entity;

import com.example.coreboard.domain.board.entity.Board;
import com.example.coreboard.domain.users.entity.Users;
import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "title", nullable = false)
    private String title;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
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
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Post(
            Board board,
            Users user,
            String title,
            String content,
            ContentFormat contentFormat
    ) {
        this.board = board;
        this.user = user;
        this.title = title;
        this.content = content;
        this.contentFormat = contentFormat;
        this.status = PostStatus.PUBLISHED;
        this.viewCount = 0L;
    }

    protected Post() {
    }

    public static Post create(
            Board board,
            Users user,
            String title,
            String content,
            ContentFormat contentFormat
    ) {
        return new Post(
                board,
                user,
                title,
                content,
                contentFormat == null ? ContentFormat.MARKDOWN : contentFormat
        );
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public PostStatus getStatus() {
        return status;
    }

    public ContentFormat getContentFormat() {
        return contentFormat;
    }

    public String getContent() {
        return content;
    }

    public String getTitle() {
        return title;
    }

    public Users getUser() {
        return user;
    }

    public Board getBoard() {
        return board;
    }

    public Long getId() {
        return id;
    }

    public void delete() {
        this.status = PostStatus.DELETED;
    }

    public boolean isWrittenBy(Users user) {
        return this.user.getUserId().equals(user.getUserId());
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
}