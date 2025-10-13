package com.example.coreboard.domain.board.entity;

import jakarta.persistence.*;
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

    @Column(name = "boardTitle", nullable = false)
    private String boardTitle;

    @Column(name = "boardContents", nullable = false, length = 1000)
    private String boardContents;

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
            String boardTitle,
            String boardContents,
            LocalDateTime createdDate,
            LocalDateTime lastModifiedDate
    ) {
        this.boardTitle = boardTitle;
        this.boardContents = boardContents;
        this.userId = userId;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    public static Board create(long userId, String boardTitle, String boardContents) {
        Board board = new Board();
        board.userId = userId;
        board.boardTitle = boardTitle;
        board.boardContents = boardContents;
        return board;
    }

    public void update(
            String newTitle,
            String newContents

    ) {
        if (newTitle != null && !newTitle.isBlank()) {
            this.boardTitle = newTitle;
        }
        if (newContents != null && !newContents.isBlank()) {
            this.boardContents = newContents;
        }
    }

    public Long getId() {
        return id;
    }

    public String getBoardTitle() {
        return boardTitle;
    }

    public String getBoardContents() {
        return boardContents;
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


