package com.example.coreboard.domain.board.Entity;

import jakarta.persistence.*;

@Entity
@Table(name="board")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="boardTitle")
    private String boardTitle;

    @Column(name="boardContents")
    private String boardContents;


}


