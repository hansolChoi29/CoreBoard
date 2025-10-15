package com.example.coreboard.domain.board.dto;

public class BoardUpdateRequest {
    // 기존 데이터라는 점에서 생성 요청 DTO와 별도로 분리하여 사용해야 함

    private final Long id;
    private final long userId;
    private final String title;
    private final String content;

    public BoardUpdateRequest(
            Long id,
            long userId,
            String title,
            String content
    ) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
    }
    public Long getId(){
        return id;
    }
    public long getUserId(){
        return userId;
    }
    public String getTitle(){
        return title;
    }
    public String getContent(){
        return content;
    }
}
