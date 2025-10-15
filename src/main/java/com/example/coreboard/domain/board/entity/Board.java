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
    @Column(name = "contents", nullable = false, length = 1000)
    private String contents;

    @Column(nullable = false)
    private long userId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(updatable = false)
    private LocalDateTime lastModifiedDate;

    protected Board() {
    }

    public Board(
            Long userId,
            String title,
            String contents,
            LocalDateTime createdDate,
            LocalDateTime lastModifiedDate
    ) {
        this.title = title;
        this.contents = contents;
        this.userId = userId;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    public static Board create(long userId, String title, String contents) {
        Board board = new Board();
        board.userId = userId;
        board.title = title;
        board.contents = contents;
        return board;
    }

    public void update(
            String newTitle,
            String newContents

    ) {
        if (newTitle != null && !newTitle.isBlank()) {
            this.title = newTitle;
        }
        if (newContents != null && !newContents.isBlank()) {
            this.contents = newContents;
        }
    }

    public Long getId() {
        return id;
    }

    public String getBoardTitle() {
        return title;
    }

    public String getBoardContents() {
        return contents;
    }

    public long getUserId() {
        return userId;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }
}


