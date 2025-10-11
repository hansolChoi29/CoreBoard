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
    private Long boardId;

    @Column(name = "boardTitle", nullable = false)
    private String boardTitle;

    @Column(name = "boardContents", nullable = false, length = 1000)
    private String boardContents;

    @Column(nullable = false)
    private String username;

    @CreatedDate
    @Column(name = "created_at", nullable = false))
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_at", nullable = false)
    private LocalDateTime lastModifiedDate;

    protected Board() {
    }

    public Board(
            String boardTitle,
            String boardContents,
            String username,
            LocalDateTime lastModifiedDate
    ) {
        this.boardTitle = boardTitle;
        this.boardContents = boardContents;
        this.username = username;
        this.lastModifiedDate = lastModifiedDate;
    }

    public Board(String title, String contents) {
    }

    public static Board createBoard(
            String boardTitle,
            String boardContents,
            String username,
            LocalDateTime lastModifiedDate
    ) {
        return new Board(boardTitle, boardContents, username, lastModifiedDate);
    }

    // 수정 용도
    public void update(String newTitle, String newContents) {
        this.boardTitle = newTitle;
        this.boardContents = newContents;
    }

    public Long getBoardId() {
        return boardId;
    }

    public String getBoardTitle() {
        return boardTitle;
    }

    public String getBoardContents() {
        return boardContents;
    }

    public String getUsername() {
        return username;
    }
    public LocalDateTime getLastModifiedDate(){
        return lastModifiedDate;
    }
}


