package com.example.coreboard.domain.comment.entity;

import com.example.coreboard.domain.post.entity.Post;
import com.example.coreboard.domain.users.entity.Users;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// 엔티티가 저장되거나 업데이트될 때 auditing 정보를 캡처하는 JPA entity listener
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
    private String content;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime lastModifiedDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentStatus status;

    protected Comment() {
    }

    public Comment(
            Post post,
            Users user,
            String content
    ) {
        this.post = post;
        this.user = user;
        this.content = content;
        this.status = CommentStatus.ACTIVE;
    }

    public static Comment create(
            Post post,
            Users user,
            String content
    ) {
        return new Comment(post, user, content);
    }

    public CommentStatus getStatus() {
        return status;
    }

    public void update(
            String content
    ) {
        this.content = content;
    }

    public void delete() {
        this.status = CommentStatus.DELETE;
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public Users getUser() {
        return user;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }
}
