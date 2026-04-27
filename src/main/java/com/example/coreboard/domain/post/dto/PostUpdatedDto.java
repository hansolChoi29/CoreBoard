package com.example.coreboard.domain.post.dto;


public class PostUpdatedDto {
    private final Long id;

    public PostUpdatedDto(
            Long id
    ) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
