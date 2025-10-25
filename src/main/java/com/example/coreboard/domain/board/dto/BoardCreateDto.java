package com.example.coreboard.domain.board.dto;

import java.time.LocalDateTime;

public class BoardCreateDto {
    // private final : 값을 한 번만 저장하고 절대 바꾸지 않겠다.
    // creat는 final 권장하지 않지만 응답용 DTO는 권장한다.
    // 이유는 서버에서 이미 값이 채워진 객체를 내보내는 상황이라 변경될 일이 없음

    private final Long id;
    private final long userId;
    private final String title;
    private final String content;
    private final LocalDateTime createdDate;

    public BoardCreateDto(
            Long id,
            long userId,
            String title,
            String content,
            LocalDateTime createdDate
    ) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.createdDate = createdDate;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public long getUserId() {
        return userId;
    }
}
