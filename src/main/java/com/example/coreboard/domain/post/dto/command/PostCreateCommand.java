package com.example.coreboard.domain.post.dto.command;

import com.example.coreboard.domain.post.entity.ContentFormat;

public class PostCreateCommand {
    private Long boardId;
    private String title;
    private String content;
    private ContentFormat contentFormat;

    public PostCreateCommand(
            Long boardId,
            String title,
            String content,
            ContentFormat contentFormat
    ) {
        this.boardId = boardId;
        this.title = title;
        this.content = content;
        this.contentFormat = contentFormat;
    }

    public ContentFormat getContentFormat() {
        return contentFormat;
    }

    public Long getBoardId() {
        return boardId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
