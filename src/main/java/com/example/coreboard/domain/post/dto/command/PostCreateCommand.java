package com.example.coreboard.domain.post.dto.command;

public class PostCreateCommand {
    private String title;
    private String content;

    public PostCreateCommand(
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
