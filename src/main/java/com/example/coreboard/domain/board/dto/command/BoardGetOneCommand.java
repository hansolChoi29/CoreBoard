package com.example.coreboard.domain.board.dto.command;

public class BoardGetOneCommand {
    private final Long id;

    public BoardGetOneCommand(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
