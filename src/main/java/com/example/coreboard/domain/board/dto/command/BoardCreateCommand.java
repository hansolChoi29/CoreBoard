package com.example.coreboard.domain.board.dto.command;

public class BoardCreateCommand {
    private String title;
    private String content;

    public BoardCreateCommand(
            String title,
            String content
    ) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
