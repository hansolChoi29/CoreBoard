package com.example.coreboard.domain.board.dto;



import java.time.LocalDateTime;

public class BoardGetOneResponse {
    private final Long id;
    private final String username;
    private final String boardTitle;
    private final String boardContents;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastModifiedDate;


    public BoardGetOneResponse(
            Long id,
            String username,
            String boardTitle,
            String boardContents,
            LocalDateTime createdDate,
            LocalDateTime lastModifiedDate
    ) {
        this.id = id;
        this.username = username;
        this.boardTitle = boardTitle;
        this.boardContents = boardContents;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getBoardTitle() {
        return boardTitle;
    }

    public String getBoardContents() {
        return boardContents;
    }

    public LocalDateTime getCreateDate() {
        return createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }
}
