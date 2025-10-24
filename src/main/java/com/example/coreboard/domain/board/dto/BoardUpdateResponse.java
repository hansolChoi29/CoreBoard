package com.example.coreboard.domain.board.dto;

import java.time.LocalDateTime;

public class BoardUpdateResponse {
    private final Long id;
//    private final long userId;
//    private final String title;
//    private final String content;
//    private final LocalDateTime lastModifiedDate;

    public BoardUpdateResponse(
            Long id
//            long userId,
//            String title,
//            String content,
//            LocalDateTime lastModifiedDate
    ) {
        this.id = id;
//        this.userId = userId;
//        this.title = title;
//        this.content = content;
//        this.lastModifiedDate = lastModifiedDate;
    }

    public Long getId() {
        return id;
    }

//    public long getUserId() {
//        return userId;
//    }
//
//    public String getTitle() {
//        return title;
//    }
//
//    public String getContent() {
//        return content;
//    }
//
//    public LocalDateTime getLastModifiedDate() {
//        return lastModifiedDate;
//    }
}
