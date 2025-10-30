package com.example.coreboard.domain.board.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    void update() {
        Board board = new Board(
                1L,
                10L,
                "기존 제목",
                "기존 내용",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        board.update("새 제목", "새 내용");

        assertEquals("새 제목", board.getTitle());
        assertEquals("새 내용", board.getContent());
    }

    @Test
    void update_isBlank() {
        Board board = new Board(
                1L,
                10L,
                "기존 데이터",
                "기존 데이터",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        board.update("","");
        assertEquals("기존 데이터", board.getTitle());
        assertEquals("기존 데이터", board.getContent());
    }
    @Test
    void update_null(){
        Board board = new Board(
                1L,
                10L,
                "기존 데이터",
                "기존 데이터",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        board.update(null,null);

        assertEquals("기존 데이터", board.getTitle());
        assertEquals("기존 데이터", board.getContent());
    }
}