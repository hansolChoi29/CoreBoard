package com.example.coreboard.domain.board.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "board")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "제목은 필수입니다")
    @Column(name = "title", nullable = false)
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private Long userId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(updatable = false)
    private LocalDateTime lastModifiedDate;

    protected Board() {
    }

    public Board(
            Long id,
            Long userId,
            String title,
            String content,
            LocalDateTime createdDate,
            LocalDateTime lastModifiedDate
    ) {
        this.id=id;
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    public static Board create(long userId, String title, String content) {
        Board board = new Board();
        board.userId = userId;
        board.title = title;
        board.content = content;
        return board;
    }

    public void update(
            String newTitle,
            String newContent

    ) {
        if (newTitle != null && !newTitle.isBlank()) {
            this.title = newTitle;
        }
        if (newContent != null && !newContent.isBlank()) {
            this.content = newContent;
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


