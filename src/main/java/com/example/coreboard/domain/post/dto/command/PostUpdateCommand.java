package com.example.coreboard.domain.post.dto.command;


import com.example.coreboard.domain.post.entity.ContentFormat;

public class PostUpdateCommand {
    private final Long id;
    private final String username;
    private final String title;
    private final String content;
    private final ContentFormat contentFormat;

    public PostUpdateCommand(
            String username,
            Long id,
            String title,
            String content,
            ContentFormat contentFormat
    ) {
        this.username = username;
        this.id = id;
        this.title = title;
        this.content = content;
        this.contentFormat = contentFormat;
    }

    public ContentFormat getContentFormat() {
        return contentFormat;
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
