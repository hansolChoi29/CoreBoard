package com.example.coreboard.domain.board.service;

import com.example.coreboard.domain.board.repository.BoardRepository;

public class Service {
    private final BoardRepository boardRepositody;

    public Service(BoardRepository boardRepositody) {
        this.boardRepositody = boardRepositody;
    }
}
