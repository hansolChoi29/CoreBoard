package com.example.coreboard.domain.board.entity;

import com.example.coreboard.domain.users.entity.Users;
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
    private String username;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(updatable = false)
    private LocalDateTime lastModifiedDate;

    protected Board() {
    }

    public Board(
            String username,
            String boardTitle,
            String boardContents,
            LocalDateTime createdDate,
            LocalDateTime lastModifiedDate
    ) {
        this.boardTitle = boardTitle;
        this.boardContents = boardContents;
        this.username = username;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    public static Board BoardCreateResponse(String username, String boardTitle, String boardContents) {
        Board board=new Board();
        board.username=username;
        board.boardTitle=boardTitle;
        board.boardContents=boardContents;
        return board;
    }

    public void update(
            String newTitle,
            String newContents

    ) {
        this.boardTitle = newTitle;
        this.boardContents = newContents;

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

    public String getUsername() {
        return username;
    }
}


