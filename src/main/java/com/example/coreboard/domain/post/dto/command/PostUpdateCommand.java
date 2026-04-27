package com.example.coreboard.domain.post.dto.command;


public class PostUpdateCommand {
    private final Long id;
    private final String username;
    private final String title;
    private final String content;

    public PostUpdateCommand(
            String username,
            Long id,
            String title,
            String content
    ) {
        this.username = username;
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
