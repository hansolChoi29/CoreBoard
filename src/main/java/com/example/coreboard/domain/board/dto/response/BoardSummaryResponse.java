package com.example.coreboard.domain.board.dto.response;

import java.time.LocalDateTime;

public class BoardSummaryResponse {
    private final Long id;
    private final long userId;
    private final String title;
    private final LocalDateTime createdDate;

    public BoardSummaryResponse(
            Long id,
            long userId,
            String title,
            LocalDateTime createdDate
    ) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.createdDate = createdDate;
    }

    public Long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
}
