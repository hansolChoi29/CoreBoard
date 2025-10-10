package com.example.coreboard.domain.board.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "board")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardid;

    @Column(name = "boardTitle", nullable = false)
    private String boardTitle;

    @Column(name = "boardContents", nullable = false, length = 1000)
    private String boardContents;

    @Column(nullable = false)
    private String username;

    protected Board() {
    }

    public Board(
            String boardTitle,
            String boardContents,
            String username
    ) {
        this.boardTitle = boardTitle;
        this.boardContents = boardContents;
        this.username = username;
    }

    public static Board createBoard(
            String boardTitle,
            String boardContents,
            String username
            ) {
        return new Board(boardTitle, boardContents, username);
    }

    public Long getBoardId() {
        return boardid;
    }

    public String getBoardTitle() {
        return boardTitle;
    }

    public String getBoardContents() {
        return boardContents;
    }
}


