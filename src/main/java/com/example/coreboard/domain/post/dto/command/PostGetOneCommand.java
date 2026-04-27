package com.example.coreboard.domain.post.dto.command;

public class PostGetOneCommand {
    private final Long id;

    public PostGetOneCommand(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
